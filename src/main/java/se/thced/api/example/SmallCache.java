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
public class SmallCache implements Cache<JsonObject> {

  private final AtomicInteger counter = new AtomicInteger(1);
  private final Map<Integer, JsonObject> cache;

  public SmallCache() {
    this.cache = createCache();
  }

  Map<Integer, JsonObject> createCache() {
    return new LRUCache<>(2);
  }

  @Override
  public Future<JsonArray> retrieve() {
    return succeededFuture(
        cache.entrySet().stream()
            .collect(
                JsonArray::new, (array, entry) -> array.add(entry.getValue()), JsonArray::addAll));
  }

  @Override
  public Future<Void> add(JsonObject value) {
    return succeededFuture(cache.put(counter.getAndIncrement(), value)).mapEmpty();
  }
}
