package com.netflix.conductor.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Any;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.netflix.conductor.common.utils.JsonMapperProvider;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonMapperProviderTest {
    @Test
    public void testSimpleMapping() throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper m = new JsonMapperProvider().get();
        assertTrue(m.canSerialize(Any.class));

        Struct struct1 = Struct.newBuilder().putFields(
                "some-key", Value.newBuilder().setStringValue("some-value").build()
        ).build();

        Any source = Any.pack(struct1);

        StringWriter buf = new StringWriter();
        m.writer().writeValue(buf, source);

        Any dest = m.reader().forType(Any.class).readValue(buf.toString());
        assertEquals(source.getTypeUrl(), dest.getTypeUrl());

        Struct struct2 = dest.unpack(Struct.class);
        assertTrue(struct2.containsFields("some-key"));
        assertEquals(
                struct1.getFieldsOrThrow("some-key").getStringValue(),
                struct2.getFieldsOrThrow("some-key").getStringValue()
        );
    }

    @Test
    public void testNullOnRead() throws IOException {
        String json = "{'a': null, 'b': 2, 'c': { 'c1': 3, 'c2': null } }".replace('\'', '"');
        ObjectMapper objectMapper = JsonMapperProvider.getInstanceWithAlways();
        Map<String, Object> data = objectMapper.readValue(json, Map.class);
        assertTrue(data.containsKey("a"));
        assertNull(data.get("a"));
        assertTrue(((Map) data.get("c")).containsKey("c2"));
        assertNull(((Map) data.get("c")).get("c2"));
    }

    @Test
    public void testNullWithDefaultMapper() throws IOException {
        String json = "{'a': null, 'b': 2, 'c': { 'c1': 3, 'c2': null } }".replace('\'', '"');
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper.readValue(json, Map.class);
        assertTrue(data.containsKey("a"));
        assertNull(data.get("a"));
        assertTrue(((Map) data.get("c")).containsKey("c2"));
        assertNull(((Map) data.get("c")).get("c2"));
    }

    @Test
    public void testNullOnWrite() throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("someKey", null);
        data.put("someId", "abc123");
        ObjectMapper objectMapper = JsonMapperProvider.getInstanceWithAlways();
        String result = objectMapper.writeValueAsString(data);
        assertTrue(result.contains("null"));
    }

    @Test
    public void testNullOnWrite2() throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("someKey", null);
        data.put("someId", "abc123");
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(data);
        System.out.println(result);
        assertTrue(result.contains("null"));
    }
}
