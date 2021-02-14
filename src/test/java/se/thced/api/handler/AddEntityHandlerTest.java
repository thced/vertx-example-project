package se.thced.api.handler;

import static io.restassured.RestAssured.given;

import se.thced.api.TestBase;
import se.thced.api.example.Cache;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.junit5.*;
import org.junit.jupiter.api.*;

/**
 * Note that this test only verify basic behavior. For a full test, consider the API test which
 * brings up the OpenAPI Router and validates input.
 *
 * @author thced
 */
@DisplayName("AddEntityHandler")
class AddEntityHandlerTest extends TestBase {

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    Router router = router(vertx);
    router.post().handler(new AddEntityHandler());

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(PORT)
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("Adding entity should return 200 OK")
  void testResponse(Vertx vertx, VertxTestContext testContext) {
    // Mock the eventbus that the handler relies on (Cache implementation).
    vertx.eventBus().consumer(Cache.ADD_TO_CACHE, message -> message.reply("OK"));

    testContext.verify(
        () ->
            given()
                .body(new JsonObject().put("name", "MyName").put("age", 38))
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(200));

    testContext.completeNow();
  }
}
