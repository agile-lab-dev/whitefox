package io.whitefox.core.types;

public class FloatType extends BasePrimitiveType {
  public static final FloatType FLOAT = new FloatType();

  private FloatType() {
    super("float");
  }
}
