package se.thced.api.handler;

import se.thced.api.example.Cache;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler that demonstrates the usage of routing context together with EventBus to dispatch a
 * message over to another Verticle for further processing. On successful processing in the "cache",
 * we will reply back to the client with a simple text; "OK". It could be a JSON or whatever else is
 * preferable.
 *
 * @author thced
 */
public class AddEntityHandler extends AbstractHandler {

  @Override
  public void handle(RoutingContext ctx) {
    final Vertx vertx = ctx.vertx();
    final JsonObject entity = ctx.getBodyAsJson();

    addEntityToCache(vertx, entity)
        // Always call "end" or "next" on context, or this will forever hang
        .onSuccess(v -> ctx.end("OK"))
        .onFailure(ctx::fail);
  }

  /**
   * Request processing by the Cache Verticle to insert the entity.
   *
   * @param vertx The vertx instance
   * @param entity The entity we want to persist
   * @return The future containing the result, or failure, of the processing
   */
  private Future<Void> addEntityToCache(Vertx vertx, JsonObject entity) {
    // Note that we use mapEmpty here, we are not interested in anything in the response, only that
    // we DID get a response (otherwise we could use "send").
    return vertx.eventBus().request(Cache.ADD_TO_CACHE, entity).mapEmpty();
  }
}
