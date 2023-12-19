package com.exemple.starter;

import com.exemple.starter.service.author.Author;
import com.exemple.starter.service.author.AuthorService;
import com.exemple.starter.service.book.Book;
import com.exemple.starter.service.book.BookService;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
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


    private AuthorService authorService;
    private BookService bookService;


    @Override
    public void start(Promise<Void> startPromise) {
        this.bookService = new BookService(vertx);
        this.authorService = new AuthorService(vertx);

        var graphQLHandlerOptions = new GraphQLHandlerOptions()
                .setRequestBatchingEnabled(true);

        var graphQL = setupGraphQL();
        var graphQLHandler = GraphQLHandler.create(graphQL, graphQLHandlerOptions);

        var options = new GraphiQLHandlerOptions()
                .setEnabled(true);

        /* Creating a HTTP router */
        var router = Router.router(vertx);
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
        var schema = vertx.fileSystem().readFileBlocking("schema.graphql").toString();

        /* (1) Parse  schema and create a TypeDefinitionRegistry */
        var schemaParser = new SchemaParser();
        var typeDefinitionRegistry = schemaParser.parse(schema);
        var runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("book", bookDataFetcher)
                        .dataFetcher("books", booksDataFetcher))
                .type(TypeRuntimeWiring.newTypeWiring("Book")
                        .dataFetcher("author", authorDatafetcher))
                .build();

        var schemaGenerator = new SchemaGenerator();
        var graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry,
                runtimeWiring);

        return GraphQL.newGraphQL(graphQLSchema).instrumentation(VertxFutureAdapter.create()).build();
    }

    private final DataFetcher<Future<Author>> authorDatafetcher = environment -> {
        Book book = environment.getSource();
        return authorService.getAuthor(book.authorId());
    };
    private final DataFetcher<Future<Book>> bookDataFetcher = environment -> bookService.getBook(environment.getArgument("id"));
    private final DataFetcher<Future<List<Book>>> booksDataFetcher = environment -> bookService.getBooks();


}
