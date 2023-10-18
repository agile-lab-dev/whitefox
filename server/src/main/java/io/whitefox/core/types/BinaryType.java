package io.whitefox.core.types;

public class BinaryType extends BasePrimitiveType {
  public static final BinaryType BINARY = new BinaryType();

  private BinaryType() {
    super("binary");
  }
}
