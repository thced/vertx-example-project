package se.thced.eventbus;

import static java.util.Objects.nonNull;

import io.reactiverse.contextual.logging.ContextualData;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryContext;
import se.thced.api.handler.CorrelationIdDecoratingHandler;

/**
 * Interceptor that reads correlation id message header and sets it on Vert.x thread context
 *
 * @param <T> The type of the message payload
 * @author thced
 */
public class InboundCorrelationIdHandler<T> implements Handler<DeliveryContext<T>> {

  @Override
  public void handle(DeliveryContext<T> event) {
    String requestId = event.message().headers().get(CorrelationIdDecoratingHandler.CORRELATION_ID);
    if (nonNull(requestId)) {
      ContextualData.put(CorrelationIdDecoratingHandler.CORRELATION_ID.toString(), requestId);
    }
    event.next();
  }
}
