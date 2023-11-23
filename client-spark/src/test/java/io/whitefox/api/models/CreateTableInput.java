package io.whitefox.api.models;

public class CreateTableInput {
  private final String name;
  private final String comment;
  private final boolean skipValidation;
  private final DeltaTableProperties properties;

  public CreateTableInput(
      String name, String comment, boolean skipValidation, DeltaTableProperties properties) {
    this.name = name;
    this.comment = comment;
    this.skipValidation = skipValidation;
    this.properties = properties;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public boolean isSkipValidation() {
    return skipValidation;
  }

  public DeltaTableProperties getProperties() {
    return properties;
  }

  public static class DeltaTableProperties {
    private final String type;
    private final String location;
    private final String additionalProperties;

    public DeltaTableProperties(String type, String location, String additionalProperties) {
      this.type = type;
      this.location = location;
      this.additionalProperties = additionalProperties;
    }

    public String getType() {
      return type;
    }

    public String getLocation() {
      return location;
    }

    public String getAdditionalProperties() {
      return additionalProperties;
    }
  }
}
