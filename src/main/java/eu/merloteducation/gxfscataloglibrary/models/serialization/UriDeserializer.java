package eu.merloteducation.gxfscataloglibrary.models.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class UriDeserializer extends StdDeserializer<String> {

    public UriDeserializer() {
        this(null);
    }

    public UriDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (node.get("@type") != null &&
                node.get("@type").textValue().equals("xsd:anyURI")) {
            return node.get("@value").textValue();
        }
        return node.textValue();
    }
}
