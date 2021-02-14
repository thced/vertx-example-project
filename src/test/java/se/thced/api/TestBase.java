package se.thced.api;

import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.junit5.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(VertxExtension.class)
public class TestBase {

  /**
   * Default HTTP port for RestAssured, Router etc
   */
  protected static final int PORT = 8080;

  @BeforeAll
  static void setup() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  /**
   * Convenience method to create a Router with BodyHandler attached.
   *
   * @param vertx The vertx instance
   * @return The router
   */
  protected static Router router(Vertx vertx) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route().handler(ResponseTimeHandler.create());
    router.route().handler(ResponseContentTypeHandler.create());

    return router;
  }

}
