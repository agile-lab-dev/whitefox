package io.whitefox.core.types.predicates;

import io.whitefox.core.ColumnRange;
import io.whitefox.core.types.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

// Only for partition values
public class EvalHelper {

  static Pair<ColumnRange, String> validateAndGetRange(List<LeafOp> children, EvalContext ctx) throws PredicateException {
    var leftChild = (ColumnOp) children.get(0);
    var columnRange = leftChild.evalExpectColumnRange(ctx);

    var rightChild = children.get(1);
    var rightType = rightChild.evalExpectValueAndType(ctx).getRight();
    var rightVal = rightChild.evalExpectValueAndType(ctx).getLeft();

    return Pair.of(columnRange, rightVal);
  }

  static Pair<Pair<ColumnRange, String>,Pair<Pair<DataType, String>, Pair<DataType, String>>> validateAndGetTypeAndValue(
      List<LeafOp> children, EvalContext ctx) throws PredicateException {
    var leftChild = children.get(0);
    var leftType = leftChild.evalExpectValueAndType(ctx).getRight();
    var leftVal = leftChild.evalExpectValueAndType(ctx).getLeft();

    var rightChild = children.get(1);
    var rightType = rightChild.evalExpectValueAndType(ctx).getRight();
    var rightVal = rightChild.evalExpectValueAndType(ctx).getLeft();
    // If the types don't match, it implies a malformed predicate tree.
    // We simply throw an exception, which will cause filtering to be skipped.
    if (!Objects.equals(leftType, rightType)) {
      throw new TypeMismatchException(leftType, rightType);
    }

    if (leftVal == null && leftChild instanceof ColumnOp){
      return Pair.of(validateAndGetRange(children, ctx), null);
    }

    // We throw an exception for nulls, which will skip filtering.
    if (leftVal == null || rightVal == null) {
      throw new NullTypeException(leftChild, rightChild);
    }
    return Pair.of(null, Pair.of(Pair.of(leftType, leftVal), Pair.of(rightType, rightVal)));
  }

  // Implements "equal" between two leaf operations.
  static Boolean equal(List<LeafOp> children, EvalContext ctx) throws PredicateException {

    var columnRangeOrTypeAndValue = validateAndGetTypeAndValue(children, ctx);
    if (columnRangeOrTypeAndValue.getLeft() != null) {
      var columnRange = columnRangeOrTypeAndValue.getLeft().getLeft();
      var value = columnRangeOrTypeAndValue.getLeft().getRight();
      return columnRange.contains(value);
    }


    var typesAndValues = columnRangeOrTypeAndValue.getRight();
    var leftType = typesAndValues.getLeft().getLeft();
    var leftVal = typesAndValues.getLeft().getRight();
    var rightVal = typesAndValues.getRight().getRight();

    if (BooleanType.BOOLEAN.equals(leftType)) {
      return Boolean.valueOf(leftVal) == Boolean.valueOf(rightVal);
    } else if (IntegerType.INTEGER.equals(leftType)) {
      return Integer.parseInt(leftVal) == Integer.parseInt(rightVal);
    } else if (LongType.LONG.equals(leftType)) {
      return Long.parseLong(leftVal) == Long.parseLong(rightVal);
    } else if (StringType.STRING.equals(leftType)) {
      return leftVal.equals(rightVal);
    } else if (DateType.DATE.equals(leftType)) {
      return Date.valueOf(leftVal).equals(Date.valueOf(rightVal));
    }
    else
      throw new TypeNotSupportedException(leftType);
  }

  // TODO: supported expressions; ie. check if column + constant
  // TODO: handle column comparisons with literals
  static Boolean lessThan(List<LeafOp> children, EvalContext ctx) throws PredicateException {


    var columnRangeOrTypeAndValue = validateAndGetTypeAndValue(children, ctx);
    if (columnRangeOrTypeAndValue.getLeft() != null) {
      var columnRange = columnRangeOrTypeAndValue.getLeft().getLeft();
      var value = columnRangeOrTypeAndValue.getLeft().getRight();
      return columnRange.contains(value);
    }

    var typesAndValues = columnRangeOrTypeAndValue.getRight();
    var leftType = typesAndValues.getLeft().getLeft();
    var leftVal = typesAndValues.getLeft().getRight();
    var rightVal = typesAndValues.getRight().getRight();

    if (IntegerType.INTEGER.equals(leftType)) {
      return Integer.parseInt(leftVal) < Integer.parseInt(rightVal);
    } else if (LongType.LONG.equals(leftType)) {
      return Long.parseLong(leftVal) < Long.parseLong(rightVal);
    } else if (StringType.STRING.equals(leftType)) {
      return leftVal.compareTo(rightVal) < 0;
    } else if (DateType.DATE.equals(leftType)) {
      return Date.valueOf(leftVal).before(Date.valueOf(rightVal));
    }
    else
      throw new TypeNotSupportedException(leftType);
  }
  // Validates that the specified value is in the correct format.
  // Throws an exception otherwise.
  public static void validateValue(String value, DataType valueType) {
    try {
      if (BooleanType.BOOLEAN.equals(valueType)) {
        Boolean.parseBoolean(value);
      } else if (IntegerType.INTEGER.equals(valueType)) {
        Integer.parseInt(value);
      } else if (LongType.LONG.equals(valueType)) {
        Long.parseLong(value);
      } else if (DateType.DATE.equals(valueType)) {
        Date.valueOf(value);
      } else if (FloatType.FLOAT.equals(valueType)) {
        Float.parseFloat(value);
      } else if (DoubleType.DOUBLE.equals(valueType)) {
        Double.parseDouble(value);
        // TODO check for non deprecated
      } else if (TimestampType.TIMESTAMP.equals(valueType)) {
        Timestamp.valueOf(value);
      } else {
        throw new TypeNotSupportedException(valueType);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Error validating " + value + " for type " + valueType + ": " + e);
    }
  }
}
