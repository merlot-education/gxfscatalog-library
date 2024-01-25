package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.merloteducation.gxfscataloglibrary.models.serialization.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SerializationTests {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void serializeString() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new StringSerializer());
        objectMapper.registerModule(module);

        String value = "somevalue";

        String serialized = objectMapper.writeValueAsString(value);
        assertTrue(serialized.contains("\"@type\":\"xsd:string\""));
        assertTrue(serialized.contains("\"@value\":\"somevalue\""));
    }

    @Test
    void serializeUri() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new UriSerializer());
        objectMapper.registerModule(module);

        String value = "somevalue";

        String serialized = objectMapper.writeValueAsString(value);
        assertTrue(serialized.contains("\"@type\":\"xsd:anyURI\""));
        assertTrue(serialized.contains("\"@value\":\"somevalue\""));
    }

    @Test
    void serializeInteger() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Integer.class, new IntegerSerializer());
        objectMapper.registerModule(module);

        int value = 5;

        String serialized = objectMapper.writeValueAsString(value);
        System.out.println(serialized);
        assertTrue(serialized.contains("\"@type\":\"xsd:integer\""));
        assertTrue(serialized.contains("\"@value\":\"5\""));
    }

    @Test
    void deserializeStringTypeValue() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StringDeserializer());
        objectMapper.registerModule(module);

        String stringTypeValue = """
                {
                    "@type": "xsd:string",
                    "@value": "somevalue"
                }
                """;
        String deserialized = objectMapper.readValue(stringTypeValue, String.class);
        assertEquals("somevalue", deserialized);
    }

    @Test
    void deserializeUriTypeValue() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new UriDeserializer());
        objectMapper.registerModule(module);

        String uriTypeValue = """
                {
                    "@type": "xsd:anyURI",
                    "@value": "somevalue"
                }
                """;
        String deserialized = objectMapper.readValue(uriTypeValue, String.class);
        assertEquals("somevalue", deserialized);
    }

    @Test
    void deserializeIntegerTypeValue() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Integer.class, new IntegerDeserializer());
        objectMapper.registerModule(module);

        String uriTypeValue = """
                {
                    "@type": "xsd:integer",
                    "@value": 5
                }
                """;
        int deserialized = objectMapper.readValue(uriTypeValue, Integer.class);
        assertEquals(5, deserialized);
    }

}
