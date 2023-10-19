package io.whitefox.api.deltasharing.server;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import io.whitefox.OpenApiValidationFilter;
import io.whitefox.api.deltasharing.encoders.DeltaPageTokenEncoder;
import io.whitefox.api.deltasharing.model.v1.generated.*;
import io.whitefox.core.Schema;
import io.whitefox.core.Share;
import io.whitefox.core.Table;
import io.whitefox.core.services.ContentAndToken;
import io.whitefox.persistence.StorageManager;
import io.whitefox.persistence.memory.InMemoryStorageManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@QuarkusTest
public class DeltaSharesApiImplTest {

  private static final String currentPath =
      Paths.get(".").toAbsolutePath().normalize().toString();
  private static final String tablePath =
      currentPath + "/src/test/resources/delta/samples/delta-table/";
  private static final String tablePathWithHistory =
      currentPath + "/src/test/resources/delta/samples/delta-table-with-history/";
  private static final MetadataResponse firstTableMetadata = new MetadataResponse()
      .metadata(new MetadataResponseMetadata()
          .id("56d48189-cdbc-44f2-9b0e-2bded4c79ed7")
          .name("table1")
          .format(new MetadataResponseMetadataFormat().provider("parquet"))
          .schemaString(
              "{\"type\":\"struct\",\"fields\":[{\"name\":\"id\",\"type\":\"long\",\"nullable\":true,\"metadata\":{}}]}")
          .partitionColumns(List.of())
          .version(0L)
          ._configuration(Map.of()));
  private static final MetadataResponse tableWithHistoryMetadata = new MetadataResponse()
      .metadata(new MetadataResponseMetadata()
          .id("56d48189-cdbc-44f2-9b0e-2bded4c79ed7")
          .name("table-with-history")
          .format(new MetadataResponseMetadataFormat().provider("parquet"))
          .schemaString(
              "{\"type\":\"struct\",\"fields\":[{\"name\":\"id\",\"type\":\"long\",\"nullable\":true,\"metadata\":{}}]}")
          .partitionColumns(List.of())
          .version(0L)
          ._configuration(Map.of()));
  private static final ProtocolResponse firstTableProtocol = new ProtocolResponse()
      .protocol(new ProtocolResponseProtocol().minReaderVersion(BigDecimal.ONE));
  private static final Set<FileObject> firstTableFiles = Set.of(
      new FileObject()
          ._file(new FileObjectFile()
              .url("file://" + tablePath
                  + "part-00003-049d1c60-7ad6-45a3-af3f-65ffcabcc974-c000.snappy.parquet")
              .id("file://" + tablePath
                  + "part-00003-049d1c60-7ad6-45a3-af3f-65ffcabcc974-c000.snappy.parquet")
              .partitionValues(Map.of())
              .size(478L)
              .stats("")
              .version(0L)
              .timestamp(1695976443161L)
              .expirationTimestamp(9223372036854775807L)),
      new FileObject()
          ._file(new FileObjectFile()
              .url("file://" + tablePath
                  + "part-00001-a67388a6-e20e-426e-a872-351c390779a5-c000.snappy.parquet")
              .id("file://" + tablePath
                  + "part-00001-a67388a6-e20e-426e-a872-351c390779a5-c000.snappy.parquet")
              .partitionValues(Map.of())
              .size(478L)
              .stats("")
              .version(0L)
              .timestamp(1695976443161L)
              .expirationTimestamp(9223372036854775807L)),
      new FileObject()
          ._file(new FileObjectFile()
              .url("file://" + tablePath
                  + "part-00007-3e861bbf-fe8b-44d0-bac7-712b8cf4608c-c000.snappy.parquet")
              .id("file://" + tablePath
                  + "part-00007-3e861bbf-fe8b-44d0-bac7-712b8cf4608c-c000.snappy.parquet")
              .partitionValues(Map.of())
              .size(478L)
              .stats("")
              .version(0L)
              .timestamp(1695976443161L)
              .expirationTimestamp(9223372036854775807L)),
      new FileObject()
          ._file(new FileObjectFile()
              .url("file://" + tablePath
                  + "part-00005-e7b9aad4-adf6-42ad-a17c-fbc93689b721-c000.snappy.parquet")
              .id("file://" + tablePath
                  + "part-00005-e7b9aad4-adf6-42ad-a17c-fbc93689b721-c000.snappy.parquet")
              .partitionValues(Map.of())
              .size(478L)
              .stats("")
              .version(0L)
              .timestamp(1695976443161L)
              .expirationTimestamp(9223372036854775807L)),
      new FileObject()
          ._file(new FileObjectFile()
              .url("file://" + tablePath
                  + "part-00009-90280af8-7b24-4519-9e49-82db78a1651b-c000.snappy.parquet")
              .id("file://" + tablePath
                  + "part-00009-90280af8-7b24-4519-9e49-82db78a1651b-c000.snappy.parquet")
              .partitionValues(Map.of())
              .size(478L)
              .stats("")
              .version(0L)
              .timestamp(1695976443161L)
              .expirationTimestamp(9223372036854775807L)));

  @BeforeAll
  public static void setup() {
    var storageManager = new InMemoryStorageManager(List.of(new Share(
        "name",
        "key",
        Map.of(
            "default",
            new Schema(
                "default",
                List.of(
                    new Table(
                        "table1",
                        "file://" + currentPath + "/src/test/resources/delta/samples/delta-table",
                        "default",
                        "name"),
                    new Table(
                        "table-with-history",
                        "file://" + currentPath
                            + "/src/test/resources/delta/samples/delta-table-with-history",
                        "default",
                        "name")),
                "name")))));
    QuarkusMock.installMockForType(storageManager, StorageManager.class);
  }

  private final DeltaPageTokenEncoder encoder;
  private final ObjectMapper objectMapper;

  @Inject
  public DeltaSharesApiImplTest(DeltaPageTokenEncoder encoder, ObjectMapper objectMapper) {
    this.encoder = encoder;
    this.objectMapper = objectMapper;
  }

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
        .queryParam("pageToken", encoder.encodePageToken(new ContentAndToken.Token(0)))
        .when()
        .filter(filter)
        .get("delta-api/v1/shares")
        .then()
        .statusCode(200)
        .body("items[0].name", is("name"))
        .body("items[0].id", is("key"))
        .body("items", is(hasSize(1)))
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
        .body("items[0].name", is("name"))
        .body("items[0].id", is("key"))
        .body("items", is(hasSize(1)))
        .body("token", is(nullValue()));
  }

  @Test
  public void listNotFoundSchemas() {
    given()
        .when()
        .filter(filter)
        .get("delta-api/v1/shares/{share}/schemas", "name1")
        .then()
        .statusCode(404)
        .body("errorCode", is("1"))
        .body("message", is("NOT FOUND"));
  }

  @Test
  public void listSchemas() {
    given()
        .when()
        .filter(filter)
        .get("delta-api/v1/shares/{share}/schemas", "name")
        .then()
        .statusCode(200)
        .body("items[0].name", is("default"))
        .body("items[0].share", is("name"))
        .body("nextPageToken", is(nullValue()));
  }

  @Test
  public void listNotExistingTablesInShare() {
    given()
        .when()
        .filter(filter)
        .get("delta-api/v1/shares/{share}/schemas/{schema}/tables", "name2", "default")
        .then()
        .statusCode(404)
        .body("errorCode", is("1"))
        .body("message", is("NOT FOUND"));
  }

  @Test
  public void listNotExistingTablesInSchema() {
    given()
        .when()
        .filter(filter)
        .get("delta-api/v1/shares/{share}/schemas/{schema}/tables", "name", "default2")
        .then()
        .statusCode(404)
        .body("errorCode", is("1"))
        .body("message", is("NOT FOUND"));
  }

  @Test
  public void listTables() {
    given()
        .when()
        .filter(filter)
        .get("delta-api/v1/shares/{share}/schemas/{schema}/tables", "name", "default")
        .then()
        .statusCode(200)
        .body("items", hasSize(2))
        .body("items[0].name", is("table1"))
        .body("items[0].schema", is("default"))
        .body("items[0].share", is("name"))
        .body("nextPageToken", is(nullValue()));
  }

  @Test
  public void tableMetadataNotFound() {
    given()
        .when()
        .filter(filter)
        .get(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/metadata",
            "name",
            "default",
            "tableNameNotFound")
        .then()
        .statusCode(404);
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  public void tableMetadata() throws IOException {
    var responseBodyLines = given()
        .when()
        .filter(filter)
        .get(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/metadata",
            "name",
            "default",
            "table1")
        .then()
        .statusCode(200)
        .extract()
        .asString()
        .split("\n");
    assertEquals(2, responseBodyLines.length);
    assertEquals(
        new ProtocolResponse()
            .protocol(new ProtocolResponseProtocol().minReaderVersion(BigDecimal.ONE)),
        objectMapper.reader().readValue(responseBodyLines[0], ProtocolResponse.class));
    assertEquals(
        new MetadataResponse()
            .metadata(new MetadataResponseMetadata()
                .id("56d48189-cdbc-44f2-9b0e-2bded4c79ed7")
                .name("table1")
                .format(new MetadataResponseMetadataFormat().provider("parquet"))
                .schemaString(
                    "{\"type\":\"struct\",\"fields\":[{\"name\":\"id\",\"type\":\"long\",\"nullable\":true,\"metadata\":{}}]}")
                .partitionColumns(List.of())
                .version(0L)
                ._configuration(Map.of())),
        objectMapper.reader().readValue(responseBodyLines[1], MetadataResponse.class));
  }

  @Test
  public void listAllTables() {
    given()
        .when()
        .filter(filter)
        .get("delta-api/v1/shares/{share}/all-tables", "name")
        .then()
        .statusCode(200)
        .body("items", hasSize(2))
        .body("items[0].name", is("table1"))
        .body("items[0].schema", is("default"))
        .body("items[0].share", is("name"))
        .body("nextPageToken", is(nullValue()));
  }

  @Test
  public void listAllOfMissingShare() {
    given()
        .when()
        .filter(filter)
        .get("delta-api/v1/shares/{share}/all-tables", "name2")
        .then()
        .statusCode(404);
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void getTableVersion() {
    given()
        .when()
        .filter(filter)
        .get(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/version",
            "name",
            "default",
            "table1")
        .then()
        .statusCode(200)
        .header("Delta-Table-Version", is("0"));
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void getTableVersionMissingTable() {
    given()
        .when()
        .filter(filter)
        .get(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/version",
            "name",
            "default",
            "table2")
        .then()
        .statusCode(404);
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void getTableVersionNotFoundTimestamp() {
    given()
        .when()
        .filter(filter)
        .queryParam("startingTimestamp", "2024-10-20T10:15:30+01:00")
        .get(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/version",
            "name",
            "default",
            "table1")
        .then()
        .statusCode(404);
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void getTableVersionBadTimestamp() {
    given()
        .when()
        .filter(filter)
        .queryParam("startingTimestamp", "acbsadqwafsdas")
        .get(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/version",
            "name",
            "default",
            "table1")
        .then()
        .statusCode(502);
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void queryTableCurrentVersion() throws IOException {
    var responseBodyLines = given()
        .when()
        .filter(filter)
        .body("{}")
        .header(new Header("Content-Type", "application/json"))
        .post(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/query",
            "name",
            "default",
            "table1")
        .then()
        .statusCode(200)
        .extract()
        .body()
        .asString()
        .split("\n");

    assertEquals(
        firstTableProtocol,
        objectMapper.reader().readValue(responseBodyLines[0], ProtocolResponse.class));
    assertEquals(
        firstTableMetadata,
        objectMapper.reader().readValue(responseBodyLines[1], MetadataResponse.class));
    var files = Arrays.stream(responseBodyLines)
        .skip(2)
        .map(line -> {
          try {
            return objectMapper.reader().readValue(line, FileObject.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toSet());
    assertEquals(files, firstTableFiles);
    assertEquals(7, responseBodyLines.length);
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void queryTableByVersion() throws IOException {
    var responseBodyLines = given()
        .when()
        .filter(filter)
        .body("{\"version\": 0}")
        .header(new Header("Content-Type", "application/json"))
        .post(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/query",
            "name",
            "default",
            "table1")
        .then()
        .statusCode(200)
        .extract()
        .body()
        .asString()
        .split("\n");

    assertEquals(
        firstTableProtocol,
        objectMapper.reader().readValue(responseBodyLines[0], ProtocolResponse.class));
    assertEquals(
        firstTableMetadata,
        objectMapper.reader().readValue(responseBodyLines[1], MetadataResponse.class));
    var files = Arrays.stream(responseBodyLines)
        .skip(2)
        .map(line -> {
          try {
            return objectMapper.reader().readValue(line, FileObject.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toSet());
    assertEquals(files, firstTableFiles);
    assertEquals(7, responseBodyLines.length);
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  public void queryTableByTs() throws IOException {
    var responseBodyLines = given()
        .when()
        .filter(filter)
        .body("{\"timestamp\": \"2023-10-03T09:15:25.899Z\"}")
        .header(new Header("Content-Type", "application/json"))
        .post(
            "delta-api/v1/shares/{share}/schemas/{schema}/tables/{table}/query",
            "name",
            "default",
            "table-with-history")
        .then()
        .statusCode(200)
        .extract()
        .body()
        .asString()
        .split("\n");

    assertEquals(
        firstTableProtocol,
        objectMapper.reader().readValue(responseBodyLines[0], ProtocolResponse.class));
    assertEquals(
        tableWithHistoryMetadata,
        objectMapper.reader().readValue(responseBodyLines[1], MetadataResponse.class));
    var files = Arrays.stream(responseBodyLines)
        .skip(2)
        .map(line -> {
          try {
            return objectMapper.reader().readValue(line, FileObject.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toSet());
    assertEquals(7, responseBodyLines.length);
  }
}
