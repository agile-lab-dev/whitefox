package io.whitefox.api.utils;

public class S3TestConfig {
  private final String region;
  private final String accessKey;
  private final String secretKey;
  private final String glueCatalogId;

  public String getRegion() {
    return region;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public String getGlueCatalogId() {
    return glueCatalogId;
  }

  public S3TestConfig(String region, String accessKey, String secretKey, String glueCatalogId) {
    this.region = region;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.glueCatalogId = glueCatalogId;
  }

  public static S3TestConfig loadFromEnv() {
    return new S3TestConfig(
        System.getenv().get("WHITEFOX_TEST_AWS_REGION"),
        System.getenv().get("WHITEFOX_TEST_AWS_ACCESS_KEY_ID"),
        System.getenv().get("WHITEFOX_TEST_AWS_SECRET_ACCESS_KEY"),
        System.getenv().get("WHITEFOX_TEST_GLUE_CATALOG_ID"));
  }
}
