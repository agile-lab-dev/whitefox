package io.whitefox.api.models;

public class S3TestConfig {

  private final String region;
  private final String accessKey;
  private final String secretKey;

  public String getRegion() {
    return region;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public S3TestConfig(String region, String accessKey, String secretKey) {
    this.region = region;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }
}
