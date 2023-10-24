package io.whitefox.api.deltasharing;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class S3TestConfig {

  private final String region;
  private final String accessKey;
  private final String secretKey;

  @Inject
  public S3TestConfig(
      @ConfigProperty(name = "whitefox.provider.aws.test.region") String region,
      @ConfigProperty(name = "whitefox.provider.aws.test.accessKey") String accessKey,
      @ConfigProperty(name = "whitefox.provider.aws.test.secretKey") String secretKey) {
    this.region = region;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }

  public String region() {
    return region;
  }

  public String accessKey() {
    return accessKey;
  }

  public String secretKey() {
    return secretKey;
  }
}
