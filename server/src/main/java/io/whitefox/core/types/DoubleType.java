package io.whitefox.core.types;

public class DoubleType extends BasePrimitiveType {
  public static final DoubleType DOUBLE = new DoubleType();

  private DoubleType() {
    super("double");
  }
}
