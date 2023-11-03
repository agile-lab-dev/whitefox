package io.whitefox.core;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class EvalHelper {

        // Implements "equal" between two leaf operations.
        static Boolean equal(List<LeafOp> children, EvalContext ctx) {
          var leftChild = children.get(0);
          var leftType = leftChild.evalExpectValueAndType(ctx).getRight();
          var leftVal = leftChild.evalExpectValueAndType(ctx).getLeft();

          var rightChild = children.get(1);
          var rightType = rightChild.evalExpectValueAndType(ctx).getRight();
          var rightVal = rightChild.evalExpectValueAndType(ctx).getLeft();
          // If the types don't match, it implies a malformed predicate tree.
          // We simply throw an exception, which will cause filtering to be skipped.
          if (!Objects.equals(leftType, rightType)) {
              throw new IllegalArgumentException(
              "Type mismatch: " + leftType + " vs " + rightType + " for " +
                  leftChild + " and " + rightChild
              );
          }

          // We throw an exception for nulls, which will skip filtering.
          if (leftVal == null || rightVal == null) {
              throw new IllegalArgumentException(
              "Comparison with null is not supported: " + leftChild + " and " + rightChild
              );
          }

          switch (leftType) {
                  case OpDataTypes.BoolType: return Boolean.valueOf(leftVal) == Boolean.valueOf(rightVal);
                  case OpDataTypes.IntType: return Integer.parseInt(leftVal) == Integer.parseInt(rightVal);
                  case OpDataTypes.LongType: return Long.parseLong(leftVal) == Long.parseLong(rightVal);
                  case OpDataTypes.StringType: return leftVal.equals(rightVal);
                  case OpDataTypes.DateType:
                  return java.sql.Date.valueOf(leftVal).equals(java.sql.Date.valueOf(rightVal));
                  default:
                        throw new IllegalArgumentException("Unsupported type: " + leftType) ;
          }
        }

        static Boolean lessThan(List<LeafOp> children, EvalContext ctx) {
                var leftChild = children.get(0);
                var leftType = leftChild.evalExpectValueAndType(ctx).getRight();
                var leftVal = leftChild.evalExpectValueAndType(ctx).getLeft();

                var rightChild = children.get(1);
                var rightType = rightChild.evalExpectValueAndType(ctx).getRight();
                var rightVal = rightChild.evalExpectValueAndType(ctx).getLeft();
                // If the types don't match, it implies a malformed predicate tree.
                // We simply throw an exception, which will cause filtering to be skipped.
                if (!Objects.equals(leftType, rightType)) {
                        throw new IllegalArgumentException(
                                "Type mismatch: " + leftType + " vs " + rightType + " for " +
                                        leftChild + " and " + rightChild
                        );
                }

                // We throw an exception for nulls, which will skip filtering.
                if (leftVal == null || rightVal == null) {
                        throw new IllegalArgumentException(
                                "Comparison with null is not supported: " + leftChild + " and " + rightChild
                        );
                }

                switch (leftType) {
//                        case OpDataTypes.BoolType: return Boolean.valueOf(leftVal) == Boolean.valueOf(rightVal);
                        case OpDataTypes.IntType: return Integer.parseInt(leftVal) < Integer.parseInt(rightVal);
                        case OpDataTypes.LongType: return Long.parseLong(leftVal) < Long.parseLong(rightVal);
                        case OpDataTypes.StringType: return leftVal.compareTo(rightVal) < 0;
                        case OpDataTypes.DateType:
                                return java.sql.Date.valueOf(leftVal).equals(java.sql.Date.valueOf(rightVal));
                        default:
                                throw new IllegalArgumentException("Unsupported type: " + leftType) ;
                }
        }
              // Validates that the specified value is in the correct format.
              // Throws an exception otherwise.
              public static void validateValue(String value, String valueType) {
              try {
                      switch (valueType) {
                              case OpDataTypes.BoolType: Boolean.parseBoolean(value);
                              case OpDataTypes.IntType: Integer.parseInt(value);
                              case OpDataTypes.LongType: Long.parseLong(value);
                              case OpDataTypes.DateType: java.sql.Date.valueOf(value);
                              case OpDataTypes.FloatType: Float.parseFloat(value);
                              case OpDataTypes.DoubleType: Double.parseDouble(value);
                              // TODO check for non deprecated
                              case OpDataTypes.TimestampType: Timestamp.parse(value);
                              default:
                      throw new IllegalArgumentException("Unsupported type: " + valueType);
                }
              } catch (Exception e) {
                      throw new IllegalArgumentException(
                      "Error validating " + value + " for type " + valueType + ": " + e
                      );
              }
              }
              }