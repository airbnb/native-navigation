package com.airbnb.android.react.navigation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public final class JacksonUtils {

    private JacksonUtils() {
    }

    public static ObjectWriter writerForType(ObjectMapper mapper, Type type) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return mapper.writerFor(javaType);
    }

    public static ObjectReader readerForType(ObjectMapper mapper, Type type) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return mapper.readerFor(javaType);
    }

    /**
     * Convenient way to read a JSON Array String into a List
     */
    public static <T> List<T> readJsonArray(ObjectMapper objectMapper, String value) {
        ObjectReader reader = JacksonUtils.readerForType(objectMapper, List.class);
        //noinspection OverlyBroadCatchBlock
        try {
            return reader.readValue(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenient way to write a List into a JSON Array string
     */
    public static <T> String writeJsonArray(ObjectMapper objectMapper, List<T> value) {
        ObjectWriter writer = JacksonUtils.writerForType(objectMapper, List.class);
        try {
            return writer.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
