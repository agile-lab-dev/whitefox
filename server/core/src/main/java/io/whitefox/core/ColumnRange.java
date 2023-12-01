package io.whitefox.core;

import java.util.Comparator;

public class ColumnRange<T> {

  T minVal;
  T maxVal;
  private final Comparator<T> ord;

  public ColumnRange(T minVal, T maxVal, Comparator<T> ord) {
    this.minVal = minVal;
    this.maxVal = maxVal;
    this.ord = ord;
  }

  public ColumnRange(T onlyVal, Comparator<T> ord) {
    this.minVal = onlyVal;
    this.maxVal = onlyVal;
    this.ord = ord;
  }

  public Boolean contains(T point) {
    var c1 = ord.compare(minVal, point);
    var c2 = ord.compare(maxVal, point);
    return (c1 <= 0 && c2 >= 0);
  }

  public static ColumnRange<Long> toLong(String minVal, String maxVal) {
    return new ColumnRange<>(Long.getLong(minVal), Long.getLong(maxVal), Comparator.naturalOrder());
  }
}
