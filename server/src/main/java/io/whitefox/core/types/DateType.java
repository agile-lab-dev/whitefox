package io.whitefox.core.types;

public class DateType extends BasePrimitiveType {
  public static final DateType DATE = new DateType();

  private DateType() {
    super("date");
  }
}
