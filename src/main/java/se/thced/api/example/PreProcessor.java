package se.thced.api.example;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.function.Function;

/**
 * A preprocessor applies a function to an input, and produces some other (future) output
 *
 * @author thced
 */
@FunctionalInterface
public interface PreProcessor extends Function<JsonObject, Future<JsonObject>> {

  /**
   * Apply the pre-processing
   *
   * @param entity The input to process
   * @return The future containing the result after process, or failure
   */
  @Override
  Future<JsonObject> apply(JsonObject entity);
}
