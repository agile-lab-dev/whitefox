package io.whitefox.core;

import io.whitefox.core.types.DataType;
import io.whitefox.core.types.IntegerType;
import io.whitefox.core.types.LongType;

import java.util.Comparator;

public class ColumnRange {

  String minVal;
  String maxVal;

  DataType valueType;

  public ColumnRange(String minVal, String maxVal, DataType valueType) {
    this.minVal = minVal;
    this.maxVal = maxVal;
    this.valueType = valueType;
  }

  public ColumnRange(String onlyVal, DataType valueType) {
    this.minVal = onlyVal;
    this.maxVal = onlyVal;
    this.valueType = valueType;
  }

  private Boolean typedContains(String point){
    if (valueType instanceof IntegerType) {
      var c1 = Integer.compare(Integer.parseInt(minVal), Integer.parseInt(point));
      var c2 = Integer.compare(Integer.parseInt(maxVal), Integer.parseInt(point));
      return (c1 <= 0 && c2 >= 0);
    }
    else if (valueType instanceof LongType){
      var c1 = Long.compare(Long.parseLong(minVal), Long.parseLong(point));
      var c2 = Long.compare(Long.parseLong(maxVal), Long.parseLong(point));
      return (c1 <= 0 && c2 >= 0);
    }
    else {
      var c1 = minVal.compareTo(point);
      var c2 = maxVal.compareTo(point);
      return (c1 <= 0 && c2 >= 0);
    }
  }

  private Boolean typedLessThan(String point){
    if (valueType instanceof IntegerType) {
      var c1 = Integer.compare(Integer.parseInt(minVal), Integer.parseInt(point));
      return (c1 < 0);
    }
    else if (valueType instanceof LongType){
      var c1 = Long.compare(Long.parseLong(minVal), Long.parseLong(point));
      return (c1 < 0);
    }
    else {
      var c = minVal.compareTo(point);
      return (c >= 0);
    }
  }

  private Boolean typedGreaterThan(String point){
    if (valueType instanceof IntegerType) {
      var c = Integer.compare(Integer.parseInt(maxVal), Integer.parseInt(point));
      return (c > 0);
    }
    else if (valueType instanceof LongType){
      var c = Long.compare(Long.parseLong(maxVal), Long.parseLong(point));
      return (c > 0);
    }
    else {
      var c = maxVal.compareTo(point);
      return (c >= 0);
    }
  }

  public Boolean contains(String point) {
    return typedContains(point);
  }

  public Boolean canBeLess(String point) {
    return typedLessThan(point);
  }

  public Boolean canBeGreater(String point) {
    return typedGreaterThan(point);
  }
}
