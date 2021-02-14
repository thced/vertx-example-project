package se.thced.api.example;

import static io.vertx.core.Future.succeededFuture;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.impl.LRUCache;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of a cache to demonstrate SPI
 *
 * @see Cache
 * @author thced
 */
public class LargeCache implements Cache<JsonObject> {

  private final AtomicInteger counter = new AtomicInteger(1);
  private final Map<Integer, JsonObject> cache;

  public LargeCache() {
    this.cache = new LRUCache<>(10);
  }

  @Override
  public Future<JsonArray> retrieve() {
    return succeededFuture(
        cache.entrySet().stream()
            .collect(
                JsonArray::new, (array, entry) -> array.add(entry.getValue()), JsonArray::addAll));
  }

  @Override
  public Future<Void> add(JsonObject entity) {
    return succeededFuture(cache.put(counter.getAndIncrement(), entity)).mapEmpty();
  }
}
