package io.github.opensabe.common.utils.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.opensabe.common.jackson.TimestampModule;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;

/**
 * 由于fastjson 1.x安全漏掉复现，如果使用了fastjson idea会报警
 * 因此暂时用jackson代替
 * <html> Vulnerable API usage
 *  <br/>
 *  CVE-2022-25845 9.8 Deserialization of Untrusted Data
 *  vulnerability with High severity found  Results powered by Checkmarx(c)
 * @author mheng
 */
@Log4j2
public final class JsonUtil {
    private static ObjectMapper objectMapper;

    static {
        //就算不在 Spring 环境中，也可以使用 ObjectMapper
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new TimestampModule());
    }

    /**
     * Spring 环境有 ObjectMapper Bean，则使用其中的 ObjectMapper
     *
     * @param objectMapper
     */
    public JsonUtil(ObjectMapper objectMapper) {
        log.info("Using Spring ObjectMapper Bean");
        JsonUtil.objectMapper = objectMapper;
    }

    @SneakyThrows
    public static String toJSONString (Object value) {
        return objectMapper.writeValueAsString(value);
    }

    @SneakyThrows
    public static <T> T parseObject (String src, Class<T> type) {
        return objectMapper.readerFor(type).readValue(src);
    }
    @SneakyThrows
    public static <T> T parseObject (byte[] content, Class<T> type) {
        return objectMapper.readerFor(type).readValue(content);
    }
    @SneakyThrows
    public static <T> T parseObject (String src, TypeReference<T> typeReference) {
        return objectMapper.readValue(src, typeReference);
    }
    @SneakyThrows
    public static <T> T parseObject (byte[] content, TypeReference<T> typeReference) {
        return objectMapper.readValue(content, typeReference);
    }
    @SneakyThrows
    public static <T> T[] parseArray (byte[] content, Class<T> type) {
        return objectMapper.readerForArrayOf(type).readValue(content);
    }
    @SneakyThrows
    public static <T> T[] parseArray (String src, Class<T> type) {
        return objectMapper.readerForArrayOf(type).readValue(src);
    }
    @SneakyThrows
    public static <T> List<T> parseList (String src, Class<T> type) {
        return objectMapper.readerForListOf(type).readValue(src);
    }
    @SneakyThrows
    public static <T> List<T> parseList (byte[] content, Class<T> type) {
        return objectMapper.readerForListOf(type).readValue(content);
    }
    @SneakyThrows
    public static <T> Map<String,T> parseMap (String src, Class<T> type) {
        return objectMapper.readerForMapOf(type).readValue(src);
    }
    @SneakyThrows
    public static <T> Map<String,T> parseMap (byte[] content, Class<T> type) {
        return objectMapper.readerForMapOf(type).readValue(content);
    }

    @SneakyThrows
    public static JsonNode parseObject (String src) {
        return objectMapper.reader().readTree(src);
    }

    @SneakyThrows
    public static JsonNode parseObject (byte[] content) {
        return objectMapper.reader().readTree(content);
    }

}