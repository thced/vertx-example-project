package se.thced.api.handler;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import se.thced.api.example.Cache;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;

/**
 * Handler that demonstrates the usage of routing context together with EventBus to dispatch a
 * message over to another Verticle for retrieval of data. On successful processing in the "cache",
 * we will reply back to the client with an array of entities (if cache is not empty..).
 *
 * @author thced
 */
public class ReturnEntitiesHandler extends AbstractHandler {

  /** A "dummy" entity to send over EventBus, since we are not allowed to send null value. */
  private static final JsonObject EMPTY = new JsonObject();

  /** The header that controls how many entities we return */
  private static final CharSequence LIMIT_ENTITIES = HttpHeaders.createOptimized("Limit-Entities");

  @Override
  public void handle(RoutingContext ctx) {
    final Vertx vertx = ctx.vertx();
    final Optional<String> maxEntities =
        Optional.ofNullable(ctx.request().headers().get(LIMIT_ENTITIES));

    requestEntitiesFromCache(vertx, maxEntities)
        .onSuccess(allEntities -> log.debug("Entites: {}", allEntities.encodePrettily()))
        .onSuccess(allEntities -> ctx.response().putHeader(CONTENT_TYPE, "application/json"))
        .onSuccess(allEntities -> ctx.end(allEntities.encodePrettily()))
        .onFailure(ctx::fail);
  }

  /**
   * Request all the entities in the cache.
   *
   * <p>Demonstrate the usage of headers over eventbus
   *
   * @param vertx The vertx instance
   * @param limit The maximum number of entities to return
   * @return The future containing the array of entities, or a failure
   */
  private Future<JsonArray> requestEntitiesFromCache(Vertx vertx, Optional<String> limit) {
    // Force "node-local" request
    DeliveryOptions options = new DeliveryOptions().setLocalOnly(true);
    // Add a possible header to reduce results
    limit.ifPresent(s -> options.addHeader("limit", s));

    return vertx
        .eventBus()
        .<JsonArray>request(Cache.RETRIEVE_FROM_CACHE, EMPTY, options)
        .map(Message::body);
  }
}
