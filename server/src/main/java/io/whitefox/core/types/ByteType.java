package io.whitefox.core.types;

public class ByteType extends BasePrimitiveType {
  public static final ByteType BYTE = new ByteType();

  private ByteType() {
    super("byte");
  }
}
