package io.whitefox.core;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class EvalContext {

    public EvalContext(Map<String, String> partitionValues, Map<String, Pair<String, String>> statsValues) {
        this.partitionValues = partitionValues;
        this.statsValues = statsValues;
    }

    Map<String, String> partitionValues;
    Map<String, Pair<String, String>> statsValues;
}

class OpDataTypes {
    static final String BoolType = "bool";
    static final String IntType = "int";
    static final String LongType = "long";
    static final String StringType = "string";
    static final String DateType = "date";
    static final String FloatType = "float";
    static final String DoubleType = "double";
    static final String TimestampType = "timestamp";

    static List<String> supportedTypes = List.of(BoolType, IntType, LongType, StringType, DateType);
    static List<String> supportedTypesV2 = Stream.concat(supportedTypes.stream(), Stream.of(FloatType, DoubleType, TimestampType)).collect(Collectors.toList());


    static Boolean isSupportedType(String valueType, Boolean forV2) {
        if (forV2) {
            return OpDataTypes.supportedTypesV2.contains(valueType);
        } else {
            return OpDataTypes.supportedTypes.contains(valueType);
        }
    }
}

interface BaseOp {
    void validate();

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


// Represents a leaf operation.
abstract class LeafOp implements BaseOp {

    abstract Boolean isNull(EvalContext ctx);

    Pair<String, String> evalExpectValueAndType(EvalContext ctx) {
        return (Pair<String, String>) eval(ctx);
    }

    @Override
    public List<BaseOp> getAllChildren() {
        return List.of();
    }

    abstract String getOpValueType();
}

// Represents a non-leaf operation.
abstract class NonLeafOp implements BaseOp {
    List<LeafOp> children;


    public List<BaseOp> getAllChildren() {
        return (List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c);
    }
}

// Represents a unary operation.
interface UnaryOp {
    // Validates number of children to be 1.
    default void validateChildren(List<BaseOp> children) {
        if (children.size() != 1)
            throw new IllegalArgumentException(
                    this + " : expected 1 but found " + children.size() + " children"
            );
        children.get(0).validate();
    }
}

interface BinaryOp {
    // Validates number of children to be 2.
    default void validateChildren(List<BaseOp> children) {
        if (children.size() != 2)
            throw new IllegalArgumentException(
                    this + " : expected 2 but found " + children.size() + " children"
            );
        children.forEach(BaseOp::validate);
        var child1 = children.get(0);
        var child2 = children.get(1);
        if (child1 instanceof LeafOp && child2 instanceof LeafOp) {
            var leftType = ((LeafOp) child1).getOpValueType();
            var rightType = ((LeafOp) child2).getOpValueType();
            if (!Objects.equals(leftType, rightType)) {
                throw new IllegalArgumentException(
                        "Type mismatch: " + leftType + " vs " + rightType + " for " +
                                child1 + " and " + child2
                );
            }
        }
    }
}

interface NaryOp {
    // Validates number of children to be at least 2.
    default void validateChildren(List<BaseOp> children) {
        if (children.size() < 2) {
            throw new IllegalArgumentException(
                    this + " : expected at least 2 but found " + children.size() + " children"
            );
        }
        children.forEach(BaseOp::validate);
    }
}

class ColumnOp extends LeafOp {

    String name;
    String valueType;

    public ColumnOp(String name, String valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    // Determine if the column value is null.
    @Override
    public Boolean isNull(EvalContext ctx) {
        return resolve(ctx) == null;
    }

    @Override
    public Boolean evalExpectBoolean(EvalContext ctx) {
        if (!Objects.equals(valueType, OpDataTypes.BoolType)) {
            throw new IllegalArgumentException(
                    "Unsupported type for boolean evaluation: " + valueType
            );
        }
        return Boolean.valueOf(resolve(ctx));
    }

    @Override
    public String getOpValueType() {
        return null;
    }

    @Override
    public Object eval(EvalContext ctx) {
        return Pair.of(resolve(ctx), valueType);
    }

    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("Name must be specified: " + this);
        }
        if (!OpDataTypes.isSupportedType(valueType, false)) {
            throw new IllegalArgumentException("Unsupported type: " + valueType);
        }
    }

    private String resolve(EvalContext ctx) {
        return ctx.partitionValues.getOrDefault(name, null);
    }
}

class LiteralOp extends LeafOp {

    String value;
    String valueType;

    @Override
    public void validate() {
        if (value == null) {
            throw new IllegalArgumentException("Value must be specified: " + this);
        }
        if (!OpDataTypes.isSupportedType(valueType, false)) {
            throw new IllegalArgumentException("Unsupported type: " + valueType);
        }
        EvalHelper.validateValue(value, valueType);
    }

    @Override
    public Object eval(EvalContext ctx) {
        return Pair.of(value, valueType);
    }

    public LiteralOp(String value, String valueType) {
        this.value = value;
        this.valueType = valueType;
    }

    @Override
    public Boolean isNull(EvalContext ctx) {
        return false;
    }


    @Override
    public String getOpValueType() {
        return valueType;
    }
}

class IsNullOp extends NonLeafOp implements UnaryOp {

    public IsNullOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {

    }

    @Override
    public Object eval(EvalContext ctx) {
        return children.get(0).isNull(ctx);
    }


}

class EqualOp extends NonLeafOp implements BinaryOp {
    List<LeafOp> children;

    public EqualOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {
        validateChildren((List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c));
    }

    @Override
    public Object eval(EvalContext ctx) {
        return EvalHelper.equal(children, ctx);
    }
}

class LessThanOp extends NonLeafOp implements BinaryOp {
    List<LeafOp> children;

    public LessThanOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {
        validateChildren((List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c));
    }

    @Override
    public Object eval(EvalContext ctx) {
        return EvalHelper.lessThan(children, ctx);
    }
}

class LessThanOrEqualOp extends NonLeafOp implements BinaryOp {

    public LessThanOrEqualOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {
        validateChildren((List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c));
    }

    @Override
    public Object eval(EvalContext ctx) {
        return EvalHelper.lessThan(children, ctx) || EvalHelper.equal(children, ctx);
    }
}


class GreaterThanOp extends NonLeafOp implements BinaryOp {

    public GreaterThanOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {
        validateChildren((List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c));
    }

    @Override
    public Object eval(EvalContext ctx) {
        return !EvalHelper.lessThan(children, ctx) && !EvalHelper.equal(children, ctx);
    }
}

class GreaterThanOrEqualOp extends NonLeafOp implements BinaryOp {

    public GreaterThanOrEqualOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {
        validateChildren((List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c));
    }

    @Override
    public Object eval(EvalContext ctx) {
        return !EvalHelper.lessThan(children, ctx);
    }
}

class AndOp extends NonLeafOp implements BinaryOp {

    public AndOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {
        validateChildren((List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c));
    }

    @Override
    public Object eval(EvalContext ctx) {
        return children.stream().allMatch(c -> c.evalExpectBoolean(ctx));
    }
}

class OrOp extends NonLeafOp implements BinaryOp {

    public OrOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {
        validateChildren((List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c));
    }

    @Override
    public Object eval(EvalContext ctx) {
        return children.stream().anyMatch(c -> c.evalExpectBoolean(ctx));
    }
}

class NotOp extends NonLeafOp implements UnaryOp {

    public NotOp(List<LeafOp> children) {
        this.children = children;
    }

    @Override
    public void validate() {
        validateChildren((List<BaseOp>) List.copyOf(children).stream().map(c -> (BaseOp) c));
    }

    @Override
    public Object eval(EvalContext ctx) {
        return !children.get(0).evalExpectBoolean(ctx);
    }
}