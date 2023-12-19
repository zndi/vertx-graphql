package com.exemple.starter.service.account;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;

public class AccountService {

  private List<Account> accounts;

  public AccountService(Vertx vertx) {
    accounts = new JsonArray(vertx.fileSystem().readFileBlocking("accounts.json")).stream().map(
      ob -> new Account(JsonObject.class.cast(ob).getString("id"),
        JsonObject.class.cast(ob).getString("label"))).collect(
      Collectors.toList());
  }

  public Future<List<Account>> getAccounts() {
    return Future.succeededFuture(this.accounts);
  }

  public Future<Account> getAccount(String idAccount) {
    return  Future.succeededFuture(
      accounts.stream().filter(a -> a.getId().equals(idAccount)).findFirst().orElse(null));
  }
}
