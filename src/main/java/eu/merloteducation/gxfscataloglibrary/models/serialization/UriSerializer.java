package eu.merloteducation.gxfscataloglibrary.models.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class UriSerializer extends StdSerializer<String> {

    public UriSerializer() {
        this(null);
    }

    public UriSerializer(Class<String> t) {
        super(t);
    }

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeString(s);
        /*jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("@type", "xsd:anyURI");
        jsonGenerator.writeStringField("@value", s);
        jsonGenerator.writeEndObject();*/
    }
}