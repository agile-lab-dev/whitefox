package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.delta.standalone.DeltaLog;
import io.delta.standalone.actions.AddFile;
import io.whitefox.core.*;
import io.whitefox.core.types.BooleanType;
import io.whitefox.core.types.DataType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.*;

import io.whitefox.core.types.DateType;
import io.whitefox.core.types.IntegerType;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.conf.Configuration;

import static io.whitefox.core.JsonPredicatesUtils.createColumnRange;
import static io.whitefox.core.types.predicates.EvaluatorVersion.V1;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "op")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ColumnOp.class, name = "column"),
  @JsonSubTypes.Type(value = LiteralOp.class, name = "literal")
})
public abstract class LeafOp implements BaseOp {

  abstract Boolean isNull(EvalContext ctx);

  @JsonProperty("valueType")
  DataType valueType;

  Pair<String, DataType> evalExpectValueAndType(EvalContext ctx) throws PredicateException {
    var res = eval(ctx);
    if (res instanceof Pair) {
      return (Pair<String, DataType>) res;
    } else {
      throw new WrongExpectedTypeException(res, Pair.class);
    }
  }

  @Override
  public List<BaseOp> getAllChildren() {
    return List.of();
  }

  abstract DataType getOpValueType();
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "column")
class ColumnOp extends LeafOp {

  @JsonProperty("name")
  String name;

  public ColumnOp() {
    super();
  }

  public ColumnOp(String name, DataType valueType) {
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
    if (!Objects.equals(valueType, BooleanType.BOOLEAN)) {
      throw new IllegalArgumentException("Unsupported type for boolean evaluation: " + valueType);
    }
    return Boolean.valueOf(resolve(ctx));
  }

  public ColumnRange evalExpectColumnRange(EvalContext ctx) {
    return createColumnRange(name, ctx, valueType);
  }

  @Override
  public DataType getOpValueType() {
    return valueType;
  }

  @Override
  public Object eval(EvalContext ctx) {
    // TODO: handle case of null column + column ranges
    return Pair.of(resolve(ctx), valueType);
  }

  public void validate() throws PredicateException {
    if (name == null) {
      throw new IllegalArgumentException("Name must be specified: " + this);
    }
    if (!this.isSupportedType(valueType, V1)) {
      throw new TypeNotSupportedException(valueType);
    }
  }

  private String resolve(EvalContext ctx) {
    // TODO: handle case of null column + column ranges
    return ctx.partitionValues.getOrDefault(name, null);
  }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "literal")
class LiteralOp extends LeafOp {
  @JsonProperty("value")
  String value;

  @Override
  public void validate() throws PredicateException {
    if (value == null) {
      throw new IllegalArgumentException("Value must be specified: " + this);
    }
    if (!isSupportedType(valueType, V1)) {
      throw new IllegalArgumentException("Unsupported type: " + valueType);
    }
    EvalHelper.validateValue(value, valueType);
  }

  @Override
  public Object eval(EvalContext ctx) {
    return Pair.of(value, valueType);
  }

  public LiteralOp() {
    super();
  }

  public LiteralOp(String value, DataType valueType) {
    this.value = value;
    this.valueType = valueType;
  }

  @Override
  public Boolean isNull(EvalContext ctx) {
    return false;
  }

  @Override
  public DataType getOpValueType() {
    return valueType;
  }
}
