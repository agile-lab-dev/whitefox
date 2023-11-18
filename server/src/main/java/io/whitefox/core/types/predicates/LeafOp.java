package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.whitefox.core.types.BooleanType;
import io.whitefox.core.types.DataType;
import org.apache.commons.lang3.tuple.Pair;

//import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Objects;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "op"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ColumnOp.class, name = "column"),
        @JsonSubTypes.Type(value = LiteralOp.class, name = "literal")}
)
//@JsonDeserialize(using = LeafOpDeserializer.class)
public abstract class LeafOp implements BaseOp {

    abstract Boolean isNull(EvalContext ctx);

    @JsonDeserialize(using = DataTypeDeserializer.class)
    @JsonProperty("valueType")
    DataType valueType;

    Pair<String, DataType> evalExpectValueAndType(EvalContext ctx) {
        return (Pair<String, DataType>) eval(ctx);
    }

    @Override
    public List<BaseOp> getAllChildren() {
        return List.of();
    }

    abstract DataType getOpValueType();
}


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "column")
class ColumnOp extends LeafOp {

    @JsonProperty("name")
    String name;

    public ColumnOp(){
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

    @Override
    public DataType getOpValueType() {
        return valueType;
    }

    @Override
    public Object eval(EvalContext ctx) {
        return Pair.of(resolve(ctx), valueType);
    }

    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("Name must be specified: " + this);
        }
        if (!this.isSupportedType(valueType, false)) {
            throw new IllegalArgumentException("Unsupported type: " + valueType);
        }
    }

    private String resolve(EvalContext ctx) {
        return ctx.partitionValues.getOrDefault(name, null);
    }
}

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "literal")
class LiteralOp extends LeafOp {
    @JsonProperty("value")
    String value;

    @Override
    public void validate() {
        if (value == null) {
            throw new IllegalArgumentException("Value must be specified: " + this);
        }
        if (!isSupportedType(valueType, false)) {
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