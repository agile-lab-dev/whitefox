package io.delta.sharing.api.server;

import static io.restassured.RestAssured.given;

import io.delta.sharing.encoders.DeltaPageTokenEncoder;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DeltaSharesApiImplTest {

  DeltaPageTokenEncoder encoder = new DeltaPageTokenEncoder();

  @Test
  public void getUnknownShare() {
    given()
        .pathParam("share", "unknownKey")
        .when()
        .get("/share/{share}")
        .then()
        .statusCode(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void listShares() throws IOException {
    given()
        .queryParam("maxResults", 50.0)
        .queryParam("pageToken", encoder.encodePageToken("0"))
        .when()
        .get("/shares")
        .then()
        .statusCode(200);
  }

  @Test
  public void listSharesNoParams() {
    given().when().get("/shares").then().statusCode(200);
  }
}
