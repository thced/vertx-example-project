package se.thced.api.handler;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import se.thced.api.TestBase;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.*;
import java.util.UUID;
import org.junit.jupiter.api.*;

@DisplayName("CorrelationIdDecoratingHandler")
class CorrelationIdDecoratingHandlerTest extends TestBase {

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    Router router = router(vertx);

    router.route().handler(CorrelationIdDecoratingHandler.create());

    router.get("/").handler(RoutingContext::end);

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(PORT)
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(5000)
  @DisplayName("Sending generated correlation id returns same id")
  void testCorrelationId(VertxTestContext testContext) {

    String correlationId = UUID.randomUUID().toString();

    testContext.verify(
        () ->
            given()
                .header(CorrelationIdDecoratingHandler.CORRELATION_ID.toString(), correlationId)
                .when()
                .get("/")
                .then()
                .assertThat()
                .header(CorrelationIdDecoratingHandler.CORRELATION_ID.toString(), is(correlationId))
                .and()
                .statusCode(200));

    testContext.completeNow();
  }

  @Test
  @Timeout(5000)
  @DisplayName("Sending no correlation id should return generated id")
  void testGeneratedCorrelationId(VertxTestContext testContext) {

    testContext.verify(
        () ->
            when()
                .get("/")
                .then()
                .assertThat()
                .header(
                    CorrelationIdDecoratingHandler.CORRELATION_ID.toString(), is(notNullValue()))
                .and()
                .statusCode(200));

    testContext.completeNow();
  }
}
