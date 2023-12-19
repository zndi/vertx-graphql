package com.exemple.starter.service.balance;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;

public class BalanceService {

  private List<Balance> balances;

  public BalanceService(Vertx vertx) {
    this.balances = new JsonArray(vertx.fileSystem().readFileBlocking("balances.json")).stream()
      .map(
        ob -> new Balance(JsonObject.class.cast(ob).getString("id"),
          JsonObject.class.cast(ob).getString("accountId"),
          JsonObject.class.cast(ob).getDouble("amount"))).collect(
        Collectors.toList());

  }


  public Future<Balance> getBalance(String idAccount) {
    return Future.succeededFuture(
      balances.stream().filter(a -> a.getAccountId().equals(idAccount)).findFirst()
        .orElse(null));
  }

}
