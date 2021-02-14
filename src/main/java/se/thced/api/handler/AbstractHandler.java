package se.thced.api.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for common behaviors in Handlers
 *
 * @author thced
 * @apiNote Consider using composition over inheritance though -- it fits nicely with Vert.x!
 */
public abstract class AbstractHandler implements Handler<RoutingContext> {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

}
