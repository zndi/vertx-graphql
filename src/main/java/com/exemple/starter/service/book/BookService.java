package com.exemple.starter.service.book;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class BookService {

    private final List<Book> books;

    public BookService(Vertx vertx) {
        books = new JsonArray(vertx.fileSystem().readFileBlocking("books.json")).stream().map(
                ob -> new Book(((JsonObject) ob).getString("id"),
                        ((JsonObject) ob).getString("title"),
                ((JsonObject) ob).getString("authorId"))).collect(
                Collectors.toList());
    }

    public Future<List<Book>> getBooks() {
        return Future.succeededFuture(this.books);
    }

    public Future<Book> getBook(String idBook) {
        return Future.succeededFuture(
                books.stream().filter(a -> a.id().equals(idBook)).findFirst().orElse(null));
    }
}
