package se.thced.api.example;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

/**
 * Example interface to demonstrate <a
 * href="https://docs.oracle.com/javase/tutorial/ext/basics/spi.html">SPI</a>
 *
 * @param <V> The type of the entities we store in the cache
 * @author thced
 */
public interface Cache<V> {

  String ADD_TO_CACHE = "add.to.cache";
  String RETRIEVE_FROM_CACHE = "retrieve.from.cache";

  Future<JsonArray> retrieve();

  Future<Void> add(V entity);
}
