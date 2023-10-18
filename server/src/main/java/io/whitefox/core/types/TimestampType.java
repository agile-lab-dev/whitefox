package io.whitefox.core.types;

public class TimestampType extends BasePrimitiveType {
  public static final TimestampType TIMESTAMP = new TimestampType();

  private TimestampType() {
    super("timestamp");
  }
}
