package io.delta.sharing.api.server;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.OpenApiValidationFilter;
import io.delta.sharing.encoders.DeltaPageTokenEncoder;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DeltaSharesApiImplTest {

  @Inject
  DeltaPageTokenEncoder encoder;

  private static final String specLocation = Paths.get(".")
      .toAbsolutePath()
      .getParent()
      .getParent()
      .resolve("docs/protocol/delta-sharing-protocol-api.yml")
      .toAbsolutePath()
      .toString();
  private static final OpenApiValidationFilter filter = new OpenApiValidationFilter(specLocation);

  @Test
  public void getUnknownShare() {
    given()
        .pathParam("share", "unknownKey")
        .when()
        .filter(filter)
        .get("delta-api/v1/shares/{share}")
        .then()
        .statusCode(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void listShares() {
    given()
        .queryParam("maxResults", 50)
        .queryParam("pageToken", encoder.encodePageToken("0"))
        .when()
        .filter(filter)
        .get("delta-api/v1/shares")
        .then()
        .statusCode(200)
        .body("items", is(empty()))
        .body("token", is(nullValue()));
  }

  @Test
  public void listSharesNoParams() {
    given()
        .when()
        .filter(filter)
        .get("delta-api/v1/shares")
        .then()
        .statusCode(200)
        .body("items", is(empty()))
        .body("nextPageToken", is(nullValue()));
  }
}
