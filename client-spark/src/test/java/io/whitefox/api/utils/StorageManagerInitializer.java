package io.whitefox.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.whitefox.api.models.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageManagerInitializer {

  private final ObjectWriter objectWriter;
  private final HttpClient httpClient;
  private final String server;
  private final EnvReader envReader;

  public StorageManagerInitializer() {
    this.objectWriter = new ObjectMapper().writer();
    this.httpClient = HttpClient.newBuilder().build();
    this.server = "http://localhost:8080";
    this.envReader = new EnvReader();
  }

  public void initStorageManager() throws JsonProcessingException, URISyntaxException {
    Stream.of(
            createStorageRequest(objectWriter),
            createProviderRequest(objectWriter),
            createTableRequest(objectWriter),
            createShareRequest(objectWriter),
            createSchemaRequest(objectWriter),
            addTableToSchemaRequest(objectWriter))
        .forEach(request -> {
          try {
            callWhiteFoxServer(httpClient, request);
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
        });
  }

  private HttpRequest addTableToSchemaRequest(ObjectWriter writer)
      throws JsonProcessingException, URISyntaxException {
    AddTableToSchemaInput addTableToSchemaInput = new AddTableToSchemaInput(
        "s3Table1", new AddTableToSchemaInput.TableReference("MrFoxProvider", "s3Table1"));
    return HttpRequest.newBuilder()
        .header("content", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(writer.writeValueAsString(addTableToSchemaInput)))
        .uri(URI.create(String.format(
            "%s/%s/%s/%s/tables", server, "/whitefox-api/v1/shares", "s3share", "s3schema")))
        .build();
  }

  private HttpRequest createSchemaRequest(ObjectWriter writer)
      throws JsonProcessingException, URISyntaxException {
    return HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.noBody())
        .header("content", "application/json")
        .uri(URI.create(
            String.format("%s/%s/%s/%s", server, "/whitefox-api/v1/shares", "s3share", "s3schema")))
        .build();
  }

  private HttpRequest createShareRequest(ObjectWriter writer)
      throws JsonProcessingException, URISyntaxException {
    CreateShareInput createShareInput =
        new CreateShareInput("s3share", "", List.of("Mr.Fox"), List.of());
    return HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(writer.writeValueAsString(createShareInput)))
        .header("content", "application/json")
        .uri(URI.create(String.format("%s/%s", server, "whitefox-api/v1/shares")))
        .build();
  }

  private HttpRequest createTableRequest(ObjectWriter writer)
      throws JsonProcessingException, URISyntaxException {
    CreateTableInput createTableInput = new CreateTableInput(
        "s3Table1",
        "",
        true,
        new CreateTableInput.DeltaTableProperties(
            "delta", "s3a://whitefox-s3-test-bucket/delta/samples/delta-table", null));

    return HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(writer.writeValueAsString(createTableInput)))
        .header("content", "application/json")
        .uri(URI.create(
            String.format("%s/%s/%s/tables", server, "whitefox-api/v1/providers", "MrFoxProvider")))
        .build();
  }

  private HttpRequest createProviderRequest(ObjectWriter writer) throws JsonProcessingException {
    ProviderInput providerInput = new ProviderInput("MrFoxProvider", "MrFoxStorage", null);
    return HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(writer.writeValueAsString(providerInput)))
        .header("content", "application/json")
        .uri(URI.create(String.format("%s/%s", server, "whitefox-api/v1/providers")))
        .build();
  }

  private HttpRequest createStorageRequest(ObjectWriter writer) throws JsonProcessingException {
    S3TestConfig s3TestConfig = envReader.readS3TestConfig();
    CreateStorage createStorage = new CreateStorage(
        "MrFoxStorage",
        "",
        "s3",
        new S3Properties(new S3Properties.AwsCredentials(
            s3TestConfig.getAccessKey(), s3TestConfig.getSecretKey(), s3TestConfig.getRegion())),
        true);
    return HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(writer.writeValueAsString(createStorage)))
        .header("content", "application/json")
        .uri(URI.create(String.format("%s/%s", server, "whitefox-api/v1/storage")))
        .build();
  }

  private void callWhiteFoxServer(HttpClient httpClient, HttpRequest httpRequest)
      throws IOException, InterruptedException {
    HttpResponse<String> response =
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    assertTrue(List.of(200, 201).contains(response.statusCode()));
  }
}
