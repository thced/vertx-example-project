package se.thced.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import se.thced.Application;
import se.thced.eventbus.InboundCorrelationIdHandler;
import se.thced.eventbus.OutboundCorrelationIdHandler;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.*;

@DisplayName("Application test(s)")
class ApplicationIntegrationTest extends TestBase {

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    vertx.eventBus().addInboundInterceptor(new InboundCorrelationIdHandler<>());
    vertx.eventBus().addOutboundInterceptor(new OutboundCorrelationIdHandler<>());

    vertx
        .deployVerticle(new Application(), new DeploymentOptions())
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(5000)
  @DisplayName("POST entity against API should succeed")
  void testPostEntity(VertxTestContext testContext) {
    String correlationId = UUID.randomUUID().toString();

    testContext.verify(
        () ->
            given()
                .header(HttpHeaders.CONTENT_TYPE.toString(), "application/json; charset=utf-8")
                .header("Correlation-ID", correlationId)
                .body(new JsonObject().put("name", "Simone").put("age", 26).encode())
                .when()
                .post("/api")
                .then()
                .assertThat()
                .header("Correlation-ID", equalTo(correlationId))
                .and()
                .statusCode(200));
    testContext.completeNow();
  }

  @Test
  @Timeout(5000)
  @DisplayName("GET against API should succeed and return entities")
  void testGetEntities(VertxTestContext testContext) {

    given()
        .headers(HttpHeaders.CONTENT_TYPE.toString(), "application/json; charset=utf-8")
        .body(new JsonObject().put("name", "Simone").put("age", 25).encode())
        .when()
        .post("/api");

    testContext.verify(
        () ->
            given()
                .header(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .when()
                .get("/api")
                .then()
                .assertThat()
                .header("Correlation-ID", not(emptyOrNullString()))
                .and()
                .header("X-Response-Time", endsWith("ms"))
                .and()
                .time(lessThan(100L), TimeUnit.MILLISECONDS)
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].age", greaterThanOrEqualTo(50))
                .body("[0].age", lessThanOrEqualTo(60))
                .body("[0].name", equalTo("SIMONE")));
    testContext.completeNow();
  }
}
