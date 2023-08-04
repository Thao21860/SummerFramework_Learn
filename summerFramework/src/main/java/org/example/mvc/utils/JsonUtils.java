package org.example.mvc.utils;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;

public class JsonUtils {
    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        // 序列化时总是包含所有属性
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // 反序列化时忽略json中不存在的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 允许序列化无属性的对象
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 序列化时日期转为iso-8601格式字符串
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static void writejson(PrintWriter pw, Object r) {
        try {
            OBJECT_MAPPER.writeValue(pw, r);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readJson(BufferedReader reader, Class<T> classType) {
        try {
            return OBJECT_MAPPER.readValue(reader, classType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
