package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.whitefox.core.types.*;

import java.util.List;
import java.util.Objects;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "op")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LeafOp.class, names = {"column", "literal"}),
//        @JsonSubTypes.Type(value = EqualOp.class, names = {"equal"/*, "not", "or", "and", "lessThan", "lessThanOrEqual", "greaterThan", "greaterThanOrEqualOp" */}),
//        @JsonSubTypes.Type(value = ColumnOp.class, name = "column"),
//        @JsonSubTypes.Type(value = LiteralOp.class, name = "literal"),
        @JsonSubTypes.Type(value = EqualOp.class, name = "equal"),
        @JsonSubTypes.Type(value = NotOp.class, name = "not"),
        @JsonSubTypes.Type(value = OrOp.class, name = "or"),
        @JsonSubTypes.Type(value = AndOp.class, name = "and"),
        @JsonSubTypes.Type(value = LessThanOp.class, name = "lessThan"),
        @JsonSubTypes.Type(value = LessThanOrEqualOp.class, name = "lessThanOrEqual"),
        @JsonSubTypes.Type(value = GreaterThanOp.class, name = "greaterThan"),
        @JsonSubTypes.Type(value = GreaterThanOrEqualOp.class, name = "greaterThanOrEqualOp")
})
public interface BaseOp {

  void validate();

  default Boolean isSupportedType(DataType valueType, Boolean forV2) {
    if (forV2) {
      return (valueType instanceof BooleanType
          || valueType instanceof IntegerType
          || valueType instanceof StringType
          || valueType instanceof DateType
          || valueType instanceof LongType
          || valueType instanceof TimestampType
          || valueType instanceof FloatType
          || valueType instanceof DoubleType);
    } else {
      return (valueType instanceof BooleanType
          || valueType instanceof IntegerType
          || valueType instanceof StringType
          || valueType instanceof DateType
          || valueType instanceof LongType);
    }
  }

  Object eval(EvalContext ctx);

  default Boolean evalExpectBoolean(EvalContext ctx) {
    return (Boolean) eval(ctx);
  }

  List<BaseOp> getAllChildren();

  default Boolean treeDepthExceeds(Integer depth) {
    if (depth <= 0) {
      return true;
    } else {
      return getAllChildren().stream().anyMatch(c -> c.treeDepthExceeds(depth - 1));
    }
  }
}


// Represents a unary operation.
interface UnaryOp {
  // Validates number of children to be 1.
  default void validateChildren(List<BaseOp> children) {
    if (children.size() != 1)
      throw new IllegalArgumentException(
          this + " : expected 1 but found " + children.size() + " children");
    children.get(0).validate();
  }
}

interface BinaryOp {
  // Validates number of children to be 2.
  default void validateChildren(List<BaseOp> children) {
    if (children.size() != 2)
      throw new IllegalArgumentException(
          this + " : expected 2 but found " + children.size() + " children");
    children.forEach(BaseOp::validate);
    var child1 = children.get(0);
    var child2 = children.get(1);
    if (child1 instanceof LeafOp && child2 instanceof LeafOp) {
      var leftType = ((LeafOp) child1).getOpValueType();
      var rightType = ((LeafOp) child2).getOpValueType();
      if (!Objects.equals(leftType, rightType)) {
        throw new IllegalArgumentException("Type mismatch: " + leftType + " vs " + rightType
            + " for " + child1 + " and " + child2);
      }
    }
  }
}

interface NaryOp {
  // Validates number of children to be at least 2.
  default void validateChildren(List<BaseOp> children) {
    if (children.size() < 2) {
      throw new IllegalArgumentException(
          this + " : expected at least 2 but found " + children.size() + " children");
    }
    children.forEach(BaseOp::validate);
  }
}
