package eu.merloteducation.gxfscataloglibrary.models.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class IntegerSerializer extends StdSerializer<Integer> {

    public IntegerSerializer() {
        this(null);
    }

    public IntegerSerializer(Class<Integer> t) {
        super(t);
    }

    @Override
    public void serialize(Integer i, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("@type", "xsd:integer");
        jsonGenerator.writeNumberField("@value", i);
        jsonGenerator.writeEndObject();
    }
}
