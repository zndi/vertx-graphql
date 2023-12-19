package com.exemple.starter.service.author;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class AuthorService {

    private final List<Author> authors;

    public AuthorService(Vertx vertx) {
        this.authors = new JsonArray(vertx.fileSystem().readFileBlocking("authors.json")).stream()
                .map(
                        ob -> new Author(((JsonObject) ob).getString("id"),
                                ((JsonObject) ob).getString("name"))).collect(
                        Collectors.toList());

    }


    public Future<Author> getAuthor(String authorId) {
        return Future.succeededFuture(
                authors.stream().filter(a -> a.id().equals(authorId)).findFirst()
                        .orElse(null));
    }

}
