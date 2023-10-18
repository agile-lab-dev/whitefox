package io.whitefox.core.types;

import java.util.Objects;

public class ArrayType extends DataType {
  private final DataType elementType;
  private final boolean containsNull;

  public ArrayType(DataType elementType, boolean containsNull) {
    this.elementType = elementType;
    this.containsNull = containsNull;
  }

  public DataType getElementType() {
    return elementType;
  }

  public boolean containsNull() {
    return containsNull;
  }

  @Override
  public boolean equivalent(DataType dataType) {
    return dataType instanceof ArrayType
        && ((ArrayType) dataType).getElementType().equivalent(elementType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArrayType arrayType = (ArrayType) o;
    return containsNull == arrayType.containsNull
        && Objects.equals(elementType, arrayType.elementType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementType, containsNull);
  }

  @Override
  public String toJson() {
    return String.format(
        "{" + "\"type\": \"array\"," + "\"elementType\": %s," + "\"containsNull\": %s" + "}",
        elementType.toJson(), containsNull);
  }

  @Override
  public String toString() {
    return "array[" + elementType + "]";
  }
}
