package se.thced.api.example;

import static io.vertx.core.Future.succeededFuture;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.VertxContextPRNG;

/**
 * Add a random amount, between 25-35, to age of the entity.
 *
 * @author thced
 */
public class AddRandomYearToAgePreProcessor implements PreProcessor {

  /** The key to the 'age' field */
  private static final String AGE = "age";

  @Override
  public Future<JsonObject> apply(JsonObject entity) {
    if (entity.containsKey(AGE)) {
      // This will throw exceptions if run by other than Vert.x threads!
      int additionalAge = VertxContextPRNG.current().nextInt(11) + 25;
      entity.put(AGE, entity.getInteger(AGE) + additionalAge);
    }
    return succeededFuture(entity);
  }
}
