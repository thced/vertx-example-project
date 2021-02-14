package se.thced.api.example;

import static io.vertx.core.Future.succeededFuture;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.ServiceHelper;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.web.common.WebEnvironment;
import io.vertx.serviceproxy.HelperUtils;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The cache holds all entities. It can be customized with different implementations of caches. In
 * this simple example, there are two differently sized caches that exemplifies this.
 *
 * @author thced
 */
public class CacheVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(CacheVerticle.class);

  private Cache<JsonObject> entityCache;

  private Collection<PreProcessor> preProcessors;

  @Override
  public void start(Promise<Void> startPromise) {
    /*
      ServiceHelper is a Vert.x abstraction over SPI, Service Provider Interface.
      You can use it to dynamically load classes that implements an interface, and are implementing
      the interface seeked.
      See 'resources/META-INF/services' to switch between the available cache implementations
    */
    Cache<JsonObject> cache = ServiceHelper.loadFactoryOrNull(Cache.class);

    if (nonNull(cache)) {
      log.debug("Cache implementation found: {}", cache.getClass().getSimpleName());
      this.entityCache = cache;
    } else {
      startPromise.fail(new NoClassDefFoundError("No Cache implementation found on classpath"));
      return;
    }

    this.preProcessors = ServiceHelper.loadFactories(PreProcessor.class);

    registerEventBusHandlers().onComplete(startPromise);
  }

  /**
   * Register consumers, eventbus handlers, that listen on addresses (e.g topics). A consumer can be
   * cluster-wide (reachable from all nodes), or it can be local (localConsumer).
   *
   * <p>A good/practical convention is to always return futures, as it makes all code behave
   * "similar".
   *
   * @return A future containing the result, or a failure
   */
  private Future<Void> registerEventBusHandlers() {
    vertx.eventBus().consumer(Cache.ADD_TO_CACHE, this::addToCache);
    vertx.eventBus().consumer(Cache.RETRIEVE_FROM_CACHE, this::retrieveFromCache);
    return succeededFuture();
  }

  /** @param message The eventbus message */
  private void retrieveFromCache(Message<JsonObject> message) {
    // In this method, we dont send anything of relevance upon retrieve in the message body

    String maxString = Optional.ofNullable(message.headers().get("max")).orElse("10");
    int max = Integer.parseInt(maxString);

    entityCache
        .retrieve()
        .map(JsonArray::stream)
        .map(stream -> stream.limit(max).collect(JsonArray::new, JsonArray::add, JsonArray::addAll))
        .onSuccess(slice -> message.reply(slice)); // no lambda, for clarity
  }

  /**
   * A Handler method that adds an entity to our cache.
   *
   * <p>In this example, a small random delay is introduced to show some resemblance to a real
   * system.
   *
   * <p>Another important part is the way the method is broken down to ease readability, which
   * otherwise suffer from the callbacks stacking up.
   *
   * @param message The eventbus message
   */
  private void addToCache(Message<JsonObject> message) {
    JsonObject entity = message.body();

    String successMessage = "OK - this will not reach the HTTP client.. we ignore it in Handler";

    vertx.setTimer(
        delay(),
        timerId ->
            apply(preProcessors, entity)
                // If all went well, add the result to cache
                // The result is the composite result of all futures we got from all pre-processors
                .onSuccess(compositeIgnored -> entityCache.add(entity))
                .onSuccess(
                    compositeIgnored ->
                        /*
                          Note that we can reply with any other object-type than handler received.
                          With great power comes great responsibility.

                          JsonObject is a convenient convention to pass around!
                        */
                        message.reply(successMessage))
                .onFailure(throwable -> message.fail(500, throwable.getMessage()))
                /*
                You CAN fail the usual way, as above, and most of the time that is sufficient.
                This shows an alternative approach, where you can add debug information back
                in the response. Message will still end up failed, but you can get custom info
                back from the error when you inspect the reply..
                Tip; import static.. or method to return Handler<Throwable> ?
                 */
                .onFailure(
                    throwable ->
                        HelperUtils.manageFailure(
                            message, throwable, WebEnvironment.development())));
  }

  /**
   * This method has side-effects (modifying 'entity'), but in Vert.x this is thread-safe since the
   * very same thread will always execute the code in a verticle.
   *
   * @param entity The entity to process
   * @return The processed entity
   */
  private Future<JsonObject> apply(Collection<PreProcessor> processors, JsonObject entity) {
    return CompositeFuture.all(
            // Apply all pre-processors to our entity
            processors.stream()
                .map(processor -> applyProcessor(processor, entity))
                .collect(toList()))
        .map(entity);
  }

  /**
   * Apply a single processor to the entity
   *
   * @param processor The processor to apply
   * @param entity The entity to apply processing to
   * @return The future containing the result of the processing, or a failure
   */
  private Future<JsonObject> applyProcessor(PreProcessor processor, JsonObject entity) {
    return processor
        .apply(entity)
        .onSuccess(
            ignore ->
                log.info("Applied processor {} to entity", processor.getClass().getSimpleName()));
  }

  /** @return a random long between 100 and 350. */
  private long delay() {
    return (long) VertxContextPRNG.current(vertx).nextInt(251) + 100;
  }
}
