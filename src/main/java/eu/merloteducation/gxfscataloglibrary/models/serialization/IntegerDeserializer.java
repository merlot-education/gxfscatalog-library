package eu.merloteducation.gxfscataloglibrary.models.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class IntegerDeserializer extends StdDeserializer<Integer> {

    public IntegerDeserializer() {
        this(null);
    }

    public IntegerDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Integer deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (node.get("@type") != null &&
                node.get("@type").textValue().equals("xsd:integer")) {
            return node.get("@value").intValue();
        }
        return node.intValue();
    }
}
