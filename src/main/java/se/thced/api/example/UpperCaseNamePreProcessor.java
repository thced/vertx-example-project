package se.thced.api.example;

import static io.vertx.core.Future.succeededFuture;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * A processor that capitalizes the name, if there exists such a key within the input
 *
 * @author thced
 */
public class UpperCaseNamePreProcessor implements PreProcessor {

  @Override
  public Future<JsonObject> apply(JsonObject entity) {
    if (entity.containsKey("name")) {
      String name = entity.getString("name");
      entity.put("name", name.toUpperCase());
    }
    return succeededFuture(entity);
  }
}
