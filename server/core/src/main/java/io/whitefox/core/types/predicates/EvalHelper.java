package io.whitefox.core.types.predicates;

import io.whitefox.core.ColumnRange;
import io.whitefox.core.types.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

// Only for partition values
public class EvalHelper {

  private static LeafEvaluationResult validateAndGetRange(
      ColumnOp columnChild, LiteralOp literalChild, EvalContext ctx) throws PredicateException {
    var columnRange = columnChild.evalExpectColumnRange(ctx);
    var rightVal = literalChild.evalExpectValueAndType(ctx).getLeft();

    return LeafEvaluationResult.createFromRange(new RangeEvaluationResult(columnRange, rightVal));
  }

  private static LeafEvaluationResult validateAndGetTypeAndValue(
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

    if (leftVal == null && leftChild instanceof ColumnOp) {
      return validateAndGetRange((ColumnOp) leftChild, (LiteralOp) rightChild, ctx);
    }

    // maybe better to enforce the Equal/LessThan... to explicitly require a column child and
    // literal child
    if (rightVal == null && rightChild instanceof ColumnOp) {
      return validateAndGetRange((ColumnOp) rightChild, (LiteralOp) leftChild, ctx);
    }

    // We throw an exception for nulls, which will skip filtering.
    if (leftVal == null || rightVal == null) {
      throw new NullTypeException(leftChild, rightChild);
    }
    return LeafEvaluationResult.createFromPartitionColumn(new PartitionEvaluationResult(
        new ColumnRange(leftVal, leftType), new ColumnRange(rightVal, rightType)));
  }

  // Implements "equal" between two leaf operations.
  static Boolean equal(List<LeafOp> children, EvalContext ctx) throws PredicateException {

    var leafEvaluationResult = validateAndGetTypeAndValue(children, ctx);
    var rangeEvaluation = leafEvaluationResult.rangeEvaluationResult.map(range -> {
      var columnRange = range.getColumnRange();
      var value = range.getValue();
      return columnRange.contains(value);
    });
    if (rangeEvaluation.isPresent()) return rangeEvaluation.get();
    else if (leafEvaluationResult.partitionEvaluationResult.isPresent()) {
      var typesAndValues = leafEvaluationResult.partitionEvaluationResult.get();
      var leftType = typesAndValues.getPartitionValue().getValueType();
      var leftVal = typesAndValues.getPartitionValue().getOnlyValue();
      var rightVal = typesAndValues.getLiteralValue().getOnlyValue();

      // we fear no exception here since it is validated before
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
      } else throw new TypeNotSupportedException(leftType);
    } else throw new PredicateColumnEvaluationException(ctx);
  }

  static Boolean lessThan(List<LeafOp> children, EvalContext ctx) throws PredicateException {

    var leafEvaluationResult = validateAndGetTypeAndValue(children, ctx);
    var rangeEvaluation = leafEvaluationResult.rangeEvaluationResult.map(range -> {
      var columnRange = range.getColumnRange();
      var value = range.getValue();
      return columnRange.canBeLess(value);
    });

    if (rangeEvaluation.isPresent()) return rangeEvaluation.get();
    else if (leafEvaluationResult.partitionEvaluationResult.isPresent()) {
      var typesAndValues = leafEvaluationResult.partitionEvaluationResult.get();
      var leftType = typesAndValues.getPartitionValue().getValueType();
      var leftVal = typesAndValues.getPartitionValue().getOnlyValue();
      var rightVal = typesAndValues.getLiteralValue().getOnlyValue();

      if (IntegerType.INTEGER.equals(leftType)) {
        return Integer.parseInt(leftVal) < Integer.parseInt(rightVal);
      } else if (LongType.LONG.equals(leftType)) {
        return Long.parseLong(leftVal) < Long.parseLong(rightVal);
      } else if (StringType.STRING.equals(leftType)) {
        return leftVal.compareTo(rightVal) < 0;
      } else if (DateType.DATE.equals(leftType)) {
        return Date.valueOf(leftVal).before(Date.valueOf(rightVal));
      } else throw new TypeNotSupportedException(leftType);
    } else throw new PredicateColumnEvaluationException(ctx);
  }

  // Validates that the specified value is in the correct format.
  // Throws an exception otherwise.
  public static void validateValue(String value, DataType valueType)
      throws TypeValidationException {
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
      } else if (TimestampType.TIMESTAMP.equals(valueType)) {
        Timestamp.valueOf(value);
      } else if (StringType.STRING.equals(valueType)) {
        return;
      } else {
        throw new TypeNotSupportedException(valueType);
      }
    } catch (Exception e) {
      throw new TypeValidationException(value, valueType);
    }
  }
}
