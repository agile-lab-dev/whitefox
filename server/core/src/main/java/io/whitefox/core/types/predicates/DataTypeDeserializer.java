package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.core.JsonParseException;
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
  public DataType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String valueType = node.asText().toUpperCase();

    switch (BasePrimitiveTypeNames.valueOf(valueType)) {
      case DATE:
        return DateType.DATE;
      case INT:
        return IntegerType.INTEGER;
      case DOUBLE:
        return DoubleType.DOUBLE;
      case FLOAT:
        return FloatType.FLOAT;
      case STRING:
        return StringType.STRING;
      case TIMESTAMP:
        return TimestampType.TIMESTAMP;
      case LONG:
        return LongType.LONG;
      default:
        throw new JsonParseException("Unknown type passed inside a json predicate: " + valueType);
    }
  }
}
