package se.thced.api.example;

import static io.vertx.core.Future.succeededFuture;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * A No-op processor that does nothing but return the input again..
 *
 * @author thced
 */
public class NoopPreProcessor implements PreProcessor {

  @Override
  public Future<JsonObject> apply(JsonObject entity) {
    return succeededFuture(entity);
  }

}
