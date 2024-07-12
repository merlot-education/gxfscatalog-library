/*
 *  Copyright 2023-2024 Dataport AöR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
        assertEquals("\"" + value + "\"", serialized);
    }

    @Test
    void serializeUri() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new UriSerializer());
        objectMapper.registerModule(module);

        String value = "somevalue";

        String serialized = objectMapper.writeValueAsString(value);
        assertEquals("\"" + value + "\"", serialized);
    }

    @Test
    void serializeInteger() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Integer.class, new IntegerSerializer());
        objectMapper.registerModule(module);

        int value = 5;

        String serialized = objectMapper.writeValueAsString(value);
        assertEquals(value, Integer.valueOf(serialized));
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
    void deserializeStringPlain() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StringDeserializer());
        objectMapper.registerModule(module);

        String stringPlain = "\"somevalue\"";
        String deserialized = objectMapper.readValue(stringPlain, String.class);
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
    void deserializeUriPlain() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new UriDeserializer());
        objectMapper.registerModule(module);

        String uriPlain = "\"somevalue\"";
        String deserialized = objectMapper.readValue(uriPlain, String.class);
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

    @Test
    void deserializeIntegerPlain() throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Integer.class, new IntegerDeserializer());
        objectMapper.registerModule(module);

        String integerPlain = "5";
        int deserialized = objectMapper.readValue(integerPlain, Integer.class);
        assertEquals(5, deserialized);
    }

}
