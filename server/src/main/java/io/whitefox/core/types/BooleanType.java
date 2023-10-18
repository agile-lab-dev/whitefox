package io.whitefox.core.types;

public class BooleanType extends BasePrimitiveType {
  public static final BooleanType BOOLEAN = new BooleanType();

  private BooleanType() {
    super("boolean");
  }
}
