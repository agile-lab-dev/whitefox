package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.whitefox.core.types.DataType;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

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
