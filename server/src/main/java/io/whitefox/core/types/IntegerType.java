package io.whitefox.core.types;

public class IntegerType extends BasePrimitiveType {
  public static final IntegerType INTEGER = new IntegerType();

  private IntegerType() {
    super("integer");
  }
}
