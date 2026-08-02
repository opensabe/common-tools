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
package io.github.opensabe.common.compat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson.JSON;

import io.github.opensabe.common.utils.json.JsonUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fastjson 持久化线格式兼容：当前/旧版 ClassLoader 交叉读写与 JsonUtil 互操作。
 */
@DisplayName("Fastjson 持久化兼容（含独立 ClassLoader 跨版本）")
class FastjsonPersistWireContractTest {

    private static SamplePayload sample() {
        LocalDateTime updated = LocalDateTime.of(2024, 6, 15, 12, 30, 45).truncatedTo(ChronoUnit.MILLIS);
        Date created = Date.from(updated.minusHours(2).atZone(ZoneId.systemDefault()).toInstant());
        return new SamplePayload(
                "id-001",
                updated,
                created,
                SamplePayload.PlainStatus.ACTIVE,
                2,
                null,
                new SamplePayload.Nested("ref-a", 1.5),
                List.of("x", "y"),
                Map.of("a", 1),
                new BigDecimal("1.25"),
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        );
    }

    private static Map<String, Object> wireMap(SamplePayload p) {
        long updatedMs = p.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("ref", p.getNested().getRef());
        nested.put("weight", p.getNested().getWeight());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("updatedAt", updatedMs);
        map.put("createdAt", p.getCreatedAt().getTime());
        map.put("status", p.getStatus().name());
        map.put("tier", p.getTier());
        map.put("nested", nested);
        map.put("labels", p.getLabels());
        map.put("counts", p.getCounts());
        map.put("ratio", p.getRatio());
        map.put("token", p.getToken().toString());
        return map;
    }

    /**
     * 当前 classpath Fastjson serialize → parseObject round-trip。
     */
    @Test
    @DisplayName("当前 Fastjson round-trip")
    void currentFastjsonRoundTrip() {
        SamplePayload original = sample();
        String stored = JSON.toJSONString(original);
        SamplePayload loaded = JSON.parseObject(stored, SamplePayload.class);
        assertEquals(original.getId(), loaded.getId());
        assertEquals(original.getCreatedAt().getTime(), loaded.getCreatedAt().getTime());
        assertEquals(original.getStatus(), loaded.getStatus());
        assertEquals(original.getNested().getRef(), loaded.getNested().getRef());
    }

    /**
     * 旧版 JAR ClassLoader 写入 → 当前 Fastjson 读取。
     */
    @Test
    @DisplayName("旧 Fastjson CL 写 → 当前 Fastjson 读")
    void oldJarWriteCurrentRead() {
        Path libs = FastjsonCompatBridge.requireCompatLibsDir();
        SamplePayload original = sample();
        String fromOld = FastjsonCompatBridge.toJsonFromMap(libs, wireMap(original));

        SamplePayload loaded = JSON.parseObject(fromOld, SamplePayload.class);
        assertEquals(original.getId(), loaded.getId());
        assertEquals(original.getCreatedAt().getTime(), loaded.getCreatedAt().getTime());
        assertEquals(original.getUpdatedAt(), loaded.getUpdatedAt());
        assertEquals(original.getStatus(), loaded.getStatus());
    }

    /**
     * 当前 Fastjson 写入 → 旧版 ClassLoader 读取为 Map。
     */
    @Test
    @DisplayName("当前 Fastjson 写 → 旧 Fastjson CL 读")
    void currentWriteOldJarRead() {
        Path libs = FastjsonCompatBridge.requireCompatLibsDir();
        SamplePayload original = sample();
        String fromCurrent = JSON.toJSONString(original);

        Map<String, Object> map = FastjsonCompatBridge.parseAsMap(libs, fromCurrent);
        assertEquals(original.getId(), map.get("id"));
        assertEquals(original.getStatus().name(), map.get("status"));
        Object created = map.get("createdAt");
        assertInstanceOf(Number.class, created);
        assertEquals(original.getCreatedAt().getTime(), ((Number) created).longValue());
    }

    /**
     * gzip+Base64 Redis 大包形态：当前 Fastjson 序列化后非裸 JSON 前缀。
     */
    @Test
    @DisplayName("gzip+Base64 大包 Redis 形态：当前 Fastjson")
    void gzipBase64RedisShape() throws Exception {
        SamplePayload original = sample();
        String json = JSON.toJSONString(original);
        String stored = gzipBase64(json);
        String restored = gunzipBase64(stored);
        SamplePayload loaded = JSON.parseObject(restored, SamplePayload.class);
        assertEquals(original.getId(), loaded.getId());
        assertTrue(stored.length() > 8);
        assertTrue(!stored.trim().startsWith("{"));
    }

    /**
     * JsonUtil 与 Fastjson 双向互读，含 LocalDateTime epoch-ms 线格式。
     */
    @Test
    @DisplayName("JsonUtil 写 → Fastjson 读；Fastjson 写 → JsonUtil 读（含 LDT）")
    void crossJsonUtilAndFastjson() {
        SamplePayload original = sample();

        String viaJsonUtil = JsonUtil.toJSONString(original);
        SamplePayload fromFastjson = JSON.parseObject(viaJsonUtil, SamplePayload.class);
        assertEquals(original.getId(), fromFastjson.getId());
        assertEquals(original.getCreatedAt().getTime(), fromFastjson.getCreatedAt().getTime());
        assertEquals(original.getUpdatedAt(), fromFastjson.getUpdatedAt());

        String viaFastjson = JSON.toJSONString(original);
        SamplePayload fromJsonUtil = JsonUtil.parseObject(viaFastjson, SamplePayload.class);
        assertEquals(original.getId(), fromJsonUtil.getId());
        assertEquals(original.getCreatedAt().getTime(), fromJsonUtil.getCreatedAt().getTime());
        assertEquals(original.getUpdatedAt(), fromJsonUtil.getUpdatedAt());
    }

    private static String gzipBase64(String json) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    private static String gunzipBase64(String stored) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(stored);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
