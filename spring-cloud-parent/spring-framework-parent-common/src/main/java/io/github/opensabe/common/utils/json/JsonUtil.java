/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.utils.json;

import java.util.List;
import java.util.Map;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.blackbird.BlackbirdModule;

import io.github.opensabe.common.jackson.TimestampModule;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * JSON 静态工具类，基于 Jackson 3 {@link ObjectMapper} 提供序列化与反序列化门面。
 * <p>
 * 非 Spring 环境使用内置独立 mapper（启用时间戳日期、{@link TimestampModule}、Blackbird）；
 * Spring 容器启动后可通过 Bean 构造器替换为容器中的 {@link ObjectMapper}。
 *
 * @author mheng
 */
@Log4j2
public final class JsonUtil {

    /** 全局共享的 ObjectMapper；Spring 环境下可能被构造器替换。 */
    private static ObjectMapper objectMapper;

    static {
        // 非 Spring 环境亦可独立使用
        objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(new BlackbirdModule())
                .addModule(new TimestampModule())
                .build();
    }

    /**
     * Spring 环境下用容器中的 {@link ObjectMapper} 替换静态 mapper。
     *
     * @param objectMapper Spring 管理的 ObjectMapper Bean
     */
    public JsonUtil(ObjectMapper objectMapper) {
        log.info("Using Spring ObjectMapper Bean");
        JsonUtil.objectMapper = objectMapper;
    }

    /**
     * 返回当前使用的 {@link ObjectMapper} 实例。
     *
     * @return 全局 ObjectMapper
     */
    public static ObjectMapper mapper() {
        return objectMapper;
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    @SneakyThrows
    public static String toJSONString(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    /**
     * 将对象序列化为 JSON 字节数组。
     *
     * @param object 待序列化对象
     * @return JSON UTF-8 字节
     */
    @SneakyThrows
    public static byte[] toJSONBytes(Object object) {
        return objectMapper.writeValueAsBytes(object);
    }

    /**
     * 将 JSON 字符串反序列化为指定类型。
     *
     * @param src  JSON 源字符串
     * @param type 目标类型
     * @param <T>  目标泛型
     * @return 反序列化结果
     */
    @SneakyThrows
    public static <T> T parseObject(String src, Class<T> type) {
        return objectMapper.readerFor(type).readValue(src);
    }

    /**
     * 将 JSON 字节数组反序列化为指定类型。
     *
     * @param content JSON 源字节
     * @param type    目标类型
     * @param <T>     目标泛型
     * @return 反序列化结果
     */
    @SneakyThrows
    public static <T> T parseObject(byte[] content, Class<T> type) {
        return objectMapper.readerFor(type).readValue(content);
    }

    /**
     * 将 JSON 字符串反序列化为泛型类型。
     *
     * @param src           JSON 源字符串
     * @param typeReference 类型引用
     * @param <T>           目标泛型
     * @return 反序列化结果
     */
    @SneakyThrows
    public static <T> T parseObject(String src, TypeReference<T> typeReference) {
        return objectMapper.readValue(src, typeReference);
    }

    /**
     * 将 JSON 字节数组反序列化为泛型类型。
     *
     * @param content       JSON 源字节
     * @param typeReference 类型引用
     * @param <T>           目标泛型
     * @return 反序列化结果
     */
    @SneakyThrows
    public static <T> T parseObject(byte[] content, TypeReference<T> typeReference) {
        return objectMapper.readValue(content, typeReference);
    }

    /**
     * 将 JSON 字节数组反序列化为对象数组。
     *
     * @param content JSON 源字节
     * @param type    元素类型
     * @param <T>     元素泛型
     * @return 对象数组
     */
    @SneakyThrows
    public static <T> T[] parseArray(byte[] content, Class<T> type) {
        return objectMapper.readerForArrayOf(type).readValue(content);
    }

    /**
     * 将 JSON 字符串反序列化为对象数组。
     *
     * @param src  JSON 源字符串
     * @param type 元素类型
     * @param <T>  元素泛型
     * @return 对象数组
     */
    @SneakyThrows
    public static <T> T[] parseArray(String src, Class<T> type) {
        return objectMapper.readerForArrayOf(type).readValue(src);
    }

    /**
     * 将 JSON 字符串反序列化为 {@link List}。
     *
     * @param src  JSON 源字符串
     * @param type 元素类型
     * @param <T>  元素泛型
     * @return 列表
     */
    @SneakyThrows
    public static <T> List<T> parseList(String src, Class<T> type) {
        return objectMapper.readerForListOf(type).readValue(src);
    }

    /**
     * 将 JSON 字节数组反序列化为 {@link List}。
     *
     * @param content JSON 源字节
     * @param type    元素类型
     * @param <T>     元素泛型
     * @return 列表
     */
    @SneakyThrows
    public static <T> List<T> parseList(byte[] content, Class<T> type) {
        return objectMapper.readerForListOf(type).readValue(content);
    }

    /**
     * 将 JSON 字符串反序列化为 {@code Map<String, T>}。
     *
     * @param src  JSON 源字符串
     * @param type 值类型
     * @param <T>  值泛型
     * @return 字符串键映射
     */
    @SneakyThrows
    public static <T> Map<String, T> parseMap(String src, Class<T> type) {
        return objectMapper.readerForMapOf(type).readValue(src);
    }

    /**
     * 将 JSON 字节数组反序列化为 {@code Map<String, T>}。
     *
     * @param content JSON 源字节
     * @param type    值类型
     * @param <T>     值泛型
     * @return 字符串键映射
     */
    @SneakyThrows
    public static <T> Map<String, T> parseMap(byte[] content, Class<T> type) {
        return objectMapper.readerForMapOf(type).readValue(content);
    }

    /**
     * 将 JSON 字符串解析为 {@link JsonNode} 树。
     *
     * @param src JSON 源字符串
     * @return JSON 树节点
     */
    @SneakyThrows
    public static JsonNode parseObject(String src) {
        return objectMapper.reader().readTree(src);
    }

    /**
     * 将 JSON 字节数组解析为 {@link JsonNode} 树。
     *
     * @param content JSON 源字节
     * @return JSON 树节点
     */
    @SneakyThrows
    public static JsonNode parseObject(byte[] content) {
        return objectMapper.reader().readTree(content);
    }

    /**
     * 判断字符串是否为合法 JSON。
     *
     * @param str 待验证字符串
     * @return {@code true} 表示可解析为 JSON 树；{@code false} 表示非法 JSON
     */
    @SneakyThrows
    public static boolean isJsonValid(String str) {
        try {
            objectMapper.readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
