package models;

import java.util.List;

public class CreateShareInput {

  private final String name;
  private final String comment;
  private final List<String> recipients;
  private final List<String> schemas;

  public CreateShareInput(
      String name, String comment, List<String> recipients, List<String> schemas) {
    this.name = name;
    this.comment = comment;
    this.recipients = recipients;
    this.schemas = schemas;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public List<String> getRecipients() {
    return recipients;
  }

  public List<String> getSchemas() {
    return schemas;
  }
}
