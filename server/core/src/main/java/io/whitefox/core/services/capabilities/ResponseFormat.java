package io.whitefox.core.services.capabilities;

public enum ResponseFormat {
  parquet("parquet"),
  delta("delta");

  private final String stringRepresentation;

  public String stringRepresentation() {
    return stringRepresentation;
  }

  ResponseFormat(String str) {
    stringRepresentation = str;
  }
}
