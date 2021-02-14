package se.thced.eventbus;

import static java.util.Objects.nonNull;

import io.reactiverse.contextual.logging.ContextualData;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryContext;
import se.thced.api.handler.CorrelationIdDecoratingHandler;

/**
 * Interceptor that adds correlation id to eventbus message header
 *
 * @param <T> The type of the message payload
 * @author thced
 */
public class OutboundCorrelationIdHandler<T> implements Handler<DeliveryContext<T>> {

  @Override
  public void handle(DeliveryContext<T> event) {
    String requestId = ContextualData.get(CorrelationIdDecoratingHandler.CORRELATION_ID.toString());
    if (nonNull(requestId)) {
      event.message().headers().add(CorrelationIdDecoratingHandler.CORRELATION_ID, requestId);
    }
    event.next();
  }
}
