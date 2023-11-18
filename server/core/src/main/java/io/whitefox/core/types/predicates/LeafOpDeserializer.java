package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.whitefox.core.types.DateType;

import java.io.IOException;

public class LeafOpDeserializer extends StdDeserializer<LeafOp> {
    public LeafOpDeserializer() {
        this(null);
    }

    public LeafOpDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LeafOp deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String valueType = node.get("valueType").asText();
        String op = node.get("op").asText();

        // For example:
        switch (op) {
            case "column":
                String name = node.get("name").asText();
//                switch (valueType)
                return new ColumnOp(name, DateType.DATE);
            case "literal":
                String value = node.get("value").asText();
                return new LiteralOp(value, DateType.DATE);
            default:
                throw new IOException("type not known");
        }
    }
}