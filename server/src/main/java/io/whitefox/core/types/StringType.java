package io.whitefox.core.types;

public class StringType extends BasePrimitiveType {
  public static final StringType STRING = new StringType();

  private StringType() {
    super("string");
  }
}
