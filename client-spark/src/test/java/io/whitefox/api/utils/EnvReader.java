package io.whitefox.api.utils;

import io.whitefox.api.models.S3TestConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class EnvReader {

  public S3TestConfig readS3TestConfig() {
    Properties properties = readProperties();
    String region = properties.getProperty("WHITEFOX_TEST_AWS_REGION");
    String accessKey = properties.getProperty("WHITEFOX_TEST_AWS_ACCESS_KEY_ID");
    String secretKey = properties.getProperty("WHITEFOX_TEST_AWS_SECRET_ACCESS_KEY");
    return new S3TestConfig(region, accessKey, secretKey);
  }

  private Properties readProperties() {
    Properties properties = new Properties();
    FileInputStream input = null;
    try {
      input = new FileInputStream(String.format(
          "%s/.env",
          Paths.get(".").toAbsolutePath().getParent().getParent().toUri().getPath()));
      properties.load(input);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        input.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return properties;
  }
}
