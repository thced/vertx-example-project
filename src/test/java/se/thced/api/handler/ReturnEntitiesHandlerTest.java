package se.thced.api.handler;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import se.thced.api.TestBase;
import se.thced.api.example.Cache;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.*;
import org.junit.jupiter.api.*;

@DisplayName("ReturnEntitiesHandler")
class ReturnEntitiesHandlerTest extends TestBase {

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    Router router = router(vertx);

    router.get().handler(new ReturnEntitiesHandler());

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(PORT)
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(5000)
  @DisplayName("Should return '200 OK' and a list of entities")
  void testReturnEntities(Vertx vertx, VertxTestContext testContext) {
    // Mock the eventbus that the handler relies on (Cache implementation).
    vertx
        .eventBus()
        .consumer(
            Cache.RETRIEVE_FROM_CACHE,
            message ->
                message.reply(
                    new JsonArray()
                        .add(new JsonObject().put("name", "Justin").put("age", 15))
                        .add(new JsonObject().put("name", "Alexa").put("age", 23))));

    testContext.verify(
        () ->
            given()
                .headers("Accept", "application/json")
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .contentType("application/json")
                .and()
                .body("size()", is(2)));

    testContext.completeNow();
  }
}
