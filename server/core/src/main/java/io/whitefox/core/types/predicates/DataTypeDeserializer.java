package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.whitefox.core.types.*;

import java.io.IOException;

public class DataTypeDeserializer extends StdDeserializer<DataType> {

    // needed for jackson
    public DataTypeDeserializer() {
        this(null);
    }

    public DataTypeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DataType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String valueType = node.asText();

        switch (valueType) {
            case "date":
                return DateType.DATE;
            case "int":
                return IntegerType.INTEGER;
            case "double":
                return DoubleType.DOUBLE;
            case "float":
                return FloatType.FLOAT;
            case "string":
                return StringType.STRING;
            case "timestamp":
                return TimestampType.TIMESTAMP;
            case "long":
                return LongType.LONG;
            default:
                throw new IOException("Unknown type passed inside a json predicate: " + valueType);
        }
    }
}