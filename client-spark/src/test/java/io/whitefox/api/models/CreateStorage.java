package io.whitefox.api.models;

public class CreateStorage {

  private final String name;
  private final String comment;
  private final String type;
  private final S3Properties properties;
  private final boolean skipValidation;

  public CreateStorage(
      String name, String comment, String type, S3Properties properties, boolean skipValidation) {
    this.name = name;
    this.comment = comment;
    this.type = type;
    this.properties = properties;
    this.skipValidation = skipValidation;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public String getType() {
    return type;
  }

  public S3Properties getProperties() {
    return properties;
  }

  public boolean isSkipValidation() {
    return skipValidation;
  }
}
