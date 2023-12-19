package com.exemple.starter;

import com.exemple.starter.service.account.Account;
import com.exemple.starter.service.account.AccountService;
import com.exemple.starter.service.balance.Balance;
import com.exemple.starter.service.balance.BalanceService;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.instrumentation.VertxFutureAdapter;
import java.util.List;

public class MainVerticle extends AbstractVerticle {


  private BalanceService balanceService;
  private AccountService accountService;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    this.accountService = new AccountService(vertx);
    this.balanceService = new BalanceService(vertx);

    GraphQLHandlerOptions graphQLHandlerOptions = new GraphQLHandlerOptions()
      .setRequestBatchingEnabled(true);

    GraphQL graphQL = setupGraphQL();
    GraphQLHandler graphQLHandler = GraphQLHandler.create(graphQL, graphQLHandlerOptions);

    GraphiQLHandlerOptions options = new GraphiQLHandlerOptions()
      .setEnabled(true);

    /* Creating a HTTP router */
    Router router = Router.router(vertx);
    router.route().handler(LoggerHandler.create());
    router.post().handler(BodyHandler.create());

    router.route("/graphql").handler(graphQLHandler);
    //playground http://localhost:8888/graphiql/
    router.route("/graphiql/*").handler(GraphiQLHandler.create(options));

    /* Error handling */
    router.errorHandler(500, ctx -> {
      ctx.failure().printStackTrace();
      ctx.response().setStatusCode(500).end();
    });

    /* Start server on port 8888 */
    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });


  }

  private GraphQL setupGraphQL() {
    /* Read the schema file from the file system. */
    String schema = vertx.fileSystem().readFileBlocking("schema.graphql").toString();

    /* (1) Parse  schema and create a TypeDefinitionRegistry */
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
    var runtimeWiring = RuntimeWiring.newRuntimeWiring()
      .type(TypeRuntimeWiring.newTypeWiring("Query")
        .dataFetcher("account", accountDataFetcher)
        .dataFetcher("accounts", accountsDataFetcher))
      .type(TypeRuntimeWiring.newTypeWiring("Account")
        .dataFetcher("balance", balanceDatafetcher))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry,
      runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema).instrumentation(VertxFutureAdapter.create()).build();
  }

  private DataFetcher<Future<Balance>> balanceDatafetcher = environment -> {
    Account account = environment.getSource();
    return balanceService.getBalance(account.getId());
  };

  private DataFetcher<Future<Account>> accountDataFetcher = environment -> accountService.getAccount(environment.getArgument("id"));

  private DataFetcher<Future<List<Account>>> accountsDataFetcher = environment -> accountService.getAccounts();


}
