package models;

public class S3Properties {
  private final AwsCredentials credentials;

  public S3Properties(AwsCredentials credentials) {
    this.credentials = credentials;
  }

  public AwsCredentials getCredentials() {
    return credentials;
  }

  public static class AwsCredentials {
    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    private final String region;

    public AwsCredentials(String awsAccessKeyId, String awsSecretAccessKey, String region) {
      this.awsAccessKeyId = awsAccessKeyId;
      this.awsSecretAccessKey = awsSecretAccessKey;
      this.region = region;
    }

    public String getAwsAccessKeyId() {
      return awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
      return awsSecretAccessKey;
    }

    public String getRegion() {
      return region;
    }
  }
}
