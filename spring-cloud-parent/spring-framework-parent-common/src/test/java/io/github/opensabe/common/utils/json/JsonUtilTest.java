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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JsonUtil 工具类的测试类
 * 测试各种场景下的 JSON 序列化和反序列化功能
 */
@DisplayName("JSON工具类测试")
class JsonUtilTest {

    /**
     * 测试基本对象的 JSON 序列化
     * 测试场景：
     * 1. 普通对象的序列化
     * 2. null 值的序列化
     */
    @Test
    @DisplayName("测试基本对象JSON序列化 - 包括null值处理")
    void testToJSONString() {
        // 测试基本对象序列化
        TestObject testObject = new TestObject("test", 123);
        String json = JsonUtil.toJSONString(testObject);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":123"));

        // 测试null值序列化
        String nullJson = JsonUtil.toJSONString(null);
        assertEquals("null", nullJson);
    }

    /**
     * 测试对象到字节数组的序列化
     * 测试场景：
     * 1. 对象序列化为字节数组
     * 2. 验证序列化结果不为空
     */
    @Test
    @DisplayName("测试对象序列化为字节数组")
    void testToJSONBytes() {
        TestObject testObject = new TestObject("test", 123);
        byte[] bytes = JsonUtil.toJSONBytes(testObject);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    /**
     * 测试 JSON 字符串到对象的反序列化
     * 测试场景：
     * 1. 字符串转对象
     * 2. 字节数组转对象
     */
    @Test
    @DisplayName("测试JSON反序列化为对象 - 字符串和字节数组")
    void testParseObject() {
        // 测试字符串转对象
        String json = "{\"name\":\"test\",\"value\":123}";
        TestObject obj = JsonUtil.parseObject(json, TestObject.class);
        assertEquals("test", obj.getName());
        assertEquals(123, obj.getValue());

        // 测试字节数组转对象
        byte[] bytes = json.getBytes();
        TestObject objFromBytes = JsonUtil.parseObject(bytes, TestObject.class);
        assertEquals("test", objFromBytes.getName());
        assertEquals(123, objFromBytes.getValue());
    }

    /**
     * 测试使用 TypeReference 进行对象反序列化
     * 测试场景：
     * 1. 使用 TypeReference 处理复杂类型
     */
    @Test
    @DisplayName("测试使用TypeReference进行对象反序列化")
    void testParseObjectWithTypeReference() {
        String json = "{\"name\":\"test\",\"value\":123}";
        TypeReference<TestObject> typeRef = new TypeReference<>() {};
        TestObject obj = JsonUtil.parseObject(json, typeRef);
        assertEquals("test", obj.getName());
        assertEquals(123, obj.getValue());
    }

    /**
     * 测试 JSON 数组的反序列化
     * 测试场景：
     * 1. 字符串转数组
     * 2. 字节数组转数组
     */
    @Test
    @DisplayName("测试JSON数组反序列化 - 字符串和字节数组")
    void testParseArray() {
        // 测试字符串转数组
        String json = "[{\"name\":\"test1\",\"value\":1},{\"name\":\"test2\",\"value\":2}]";
        TestObject[] array = JsonUtil.parseArray(json, TestObject.class);
        assertEquals(2, array.length);
        assertEquals("test1", array[0].getName());
        assertEquals("test2", array[1].getName());

        // 测试字节数组转数组
        byte[] bytes = json.getBytes();
        TestObject[] arrayFromBytes = JsonUtil.parseArray(bytes, TestObject.class);
        assertEquals(2, arrayFromBytes.length);
    }

    /**
     * 测试 JSON 数组到 List 的反序列化
     * 测试场景：
     * 1. 字符串转 List
     * 2. 验证 List 中的对象属性
     */
    @Test
    @DisplayName("测试JSON数组反序列化为List")
    void testParseList() {
        String json = "[{\"name\":\"test1\",\"value\":1},{\"name\":\"test2\",\"value\":2}]";
        List<TestObject> list = JsonUtil.parseList(json, TestObject.class);
        assertEquals(2, list.size());
        assertEquals("test1", list.get(0).getName());
        assertEquals("test2", list.get(1).getName());
    }

    /**
     * 测试 JSON 到 Map 的反序列化
     * 测试场景：
     * 1. 字符串转 Map
     * 2. 验证 Map 中的对象属性
     */
    @Test
    @DisplayName("测试JSON反序列化为Map")
    void testParseMap() {
        String json = "{\"key1\":{\"name\":\"test1\",\"value\":1},\"key2\":{\"name\":\"test2\",\"value\":2}}";
        Map<String, TestObject> map = JsonUtil.parseMap(json, TestObject.class);
        assertEquals(2, map.size());
        assertEquals("test1", map.get("key1").getName());
        assertEquals("test2", map.get("key2").getName());
    }

    /**
     * 测试 JSON 到 JsonNode 的解析
     * 测试场景：
     * 1. 字符串转 JsonNode
     * 2. 验证 JsonNode 的属性访问
     */
    @Test
    @DisplayName("测试JSON解析为JsonNode")
    void testParseObjectToJsonNode() {
        String json = "{\"name\":\"test\",\"value\":123}";
        JsonNode node = JsonUtil.parseObject(json);
        assertTrue(node.has("name"));
        assertTrue(node.has("value"));
        assertEquals("test", node.get("name").asText());
        assertEquals(123, node.get("value").asInt());
    }

    /**
     * 测试 JSON 字符串的有效性验证
     * 测试场景：
     * 1. 有效 JSON 字符串
     * 2. 无效 JSON 字符串
     * 3. 空字符串
     * 4. null 值
     */
    @Test
    @DisplayName("测试JSON字符串有效性验证")
    void testIsJsonValid() {
        // 测试有效的JSON
        assertTrue(JsonUtil.isJsonValid("{\"name\":\"test\"}"));
        assertTrue(JsonUtil.isJsonValid("[1,2,3]"));
        assertTrue(JsonUtil.isJsonValid("null"));
        assertTrue(JsonUtil.isJsonValid(""));

        // 测试无效的JSON
        assertFalse(JsonUtil.isJsonValid("invalid json"));
        assertFalse(JsonUtil.isJsonValid("{name:test}"));
    }

    /**
     * 测试日期时间类型的序列化和反序列化
     * 测试场景：
     * 1. LocalDateTime 的序列化
     * 2. LocalDateTime 的反序列化
     */
    @Test
    @DisplayName("测试日期时间类型序列化和反序列化")
    void testDateTimeHandling() {
        // 测试日期时间序列化和反序列化
        DateTimeObject dateTimeObject = new DateTimeObject(LocalDateTime.now());
        String json = JsonUtil.toJSONString(dateTimeObject);
        DateTimeObject parsed = JsonUtil.parseObject(json, DateTimeObject.class);
        assertNotNull(parsed.getDateTime());
    }

    /**
     * 测试 Record 类型的序列化和反序列化
     * 测试场景：
     * 1. 基本 Record 的序列化
     * 2. Record 的反序列化
     * 3. 验证 Record 的属性访问
     */
    @Test
    @DisplayName("测试Record类型序列化和反序列化")
    void testRecordSerialization() {
        // 测试基本 record 序列化
        BasicRecord record = new BasicRecord("test", 123);
        String json = JsonUtil.toJSONString(record);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":123"));

        // 测试 record 反序列化
        BasicRecord parsed = JsonUtil.parseObject(json, BasicRecord.class);
        assertEquals("test", parsed.name());
        assertEquals(123, parsed.value());
    }

    /**
     * 测试嵌套 Record 的序列化和反序列化
     * 测试场景：
     * 1. Record 中包含其他 Record
     * 2. 多层 Record 嵌套
     * 3. 验证嵌套 Record 的属性访问
     */
    @Test
    @DisplayName("测试嵌套Record序列化和反序列化")
    void testNestedRecordSerialization() {
        // 测试嵌套 record 序列化
        NestedRecord nested = new NestedRecord(
            new BasicRecord("test", 123),
            "description"
        );
        String json = JsonUtil.toJSONString(nested);
        assertTrue(json.contains("\"basic\":{\"name\":\"test\",\"value\":123}"));
        assertTrue(json.contains("\"description\":\"description\""));

        // 测试嵌套 record 反序列化
        NestedRecord parsed = JsonUtil.parseObject(json, NestedRecord.class);
        assertEquals("test", parsed.basic().name());
        assertEquals(123, parsed.basic().value());
        assertEquals("description", parsed.description());
    }

    /**
     * 测试类中包含 Record 的序列化和反序列化
     * 测试场景：
     * 1. 普通类中包含 Record 类型字段
     * 2. 验证 Record 字段的序列化和反序列化
     */
    @Test
    @DisplayName("测试类中包含Record序列化和反序列化")
    void testClassWithRecordSerialization() {
        // 测试 class 中包含 record 的序列化
        ClassWithRecord classWithRecord = new ClassWithRecord(
            new BasicRecord("test", 123),
            "additional"
        );
        String json = JsonUtil.toJSONString(classWithRecord);
        assertTrue(json.contains("\"record\":{\"name\":\"test\",\"value\":123}"));
        assertTrue(json.contains("\"additional\":\"additional\""));

        // 测试 class 中包含 record 的反序列化
        ClassWithRecord parsed = JsonUtil.parseObject(json, ClassWithRecord.class);
        assertEquals("test", parsed.getRecord().name());
        assertEquals(123, parsed.getRecord().value());
        assertEquals("additional", parsed.getAdditional());
    }

    /**
     * 测试 Record 中包含类的序列化和反序列化
     * 测试场景：
     * 1. Record 中包含普通类类型字段
     * 2. 验证类字段的序列化和反序列化
     */
    @Test
    @DisplayName("测试Record中包含类序列化和反序列化")
    void testRecordWithClassSerialization() {
        // 测试 record 中包含 class 的序列化
        RecordWithClass recordWithClass = new RecordWithClass(
            new TestObject("test", 123),
            "metadata"
        );
        String json = JsonUtil.toJSONString(recordWithClass);
        assertTrue(json.contains("\"object\":{\"name\":\"test\",\"value\":123}"));
        assertTrue(json.contains("\"metadata\":\"metadata\""));

        // 测试 record 中包含 class 的反序列化
        RecordWithClass parsed = JsonUtil.parseObject(json, RecordWithClass.class);
        assertEquals("test", parsed.object().getName());
        assertEquals(123, parsed.object().getValue());
        assertEquals("metadata", parsed.metadata());
    }

    /**
     * 测试复杂集合类型的序列化和反序列化
     * 测试场景：
     * 1. List 中包含复杂对象
     * 2. Map 中包含复杂对象
     * 3. Set 的使用
     * 4. 验证集合中对象的属性
     */
    @Test
    @DisplayName("测试复杂集合类型序列化和反序列化")
    void testComplexCollectionTypes() {
        // 测试复杂集合类型
        ComplexCollectionObject complex = new ComplexCollectionObject(
            List.of(new TestObject("item1", 1), new TestObject("item2", 2)),
            Map.of("key1", new TestObject("value1", 1), "key2", new TestObject("value2", 2)),
            Set.of("set1", "set2")
        );

        String json = JsonUtil.toJSONString(complex);
        ComplexCollectionObject parsed = JsonUtil.parseObject(json, ComplexCollectionObject.class);

        assertEquals(2, parsed.getList().size());
        assertEquals(2, parsed.getMap().size());
        assertEquals(2, parsed.getSet().size());
    }

    /**
     * 测试枚举类型的序列化和反序列化
     * 测试场景：
     * 1. 枚举值的序列化
     * 2. 枚举值的反序列化
     * 3. 验证枚举值的正确性
     */
    @Test
    @DisplayName("测试枚举类型序列化和反序列化")
    void testEnumTypes() {
        // 测试枚举类型
        EnumObject enumObject = new EnumObject(Status.ACTIVE, "test");
        String json = JsonUtil.toJSONString(enumObject);
        EnumObject parsed = JsonUtil.parseObject(json, EnumObject.class);

        assertEquals(Status.ACTIVE, parsed.getStatus());
        assertEquals("test", parsed.getName());
    }

    /**
     * 测试泛型类型的序列化和反序列化
     * 测试场景：
     * 1. 泛型容器的序列化
     * 2. 使用 TypeReference 处理泛型
     * 3. 验证泛型类型参数
     */
    @Test
    @DisplayName("测试泛型类型序列化和反序列化")
    void testGenericTypes() {
        // 测试泛型类型
        GenericContainer<String> stringContainer = new GenericContainer<>("test", 1);
        String json = JsonUtil.toJSONString(stringContainer);
        GenericContainer<String> parsed = JsonUtil.parseObject(json, new TypeReference<>() {
        });

        assertEquals("test", parsed.getData());
        assertEquals(1, parsed.getVersion());
    }

    /**
     * 测试多层级嵌套对象的序列化和反序列化
     * 测试场景：
     * 1. 三层嵌套对象结构
     * 2. 验证深层嵌套属性的访问
     * 3. 复杂对象图的序列化
     */
    @Test
    @DisplayName("测试多层级嵌套对象序列化和反序列化")
    void testMultiLevelNesting() {
        // 测试多层级嵌套
        MultiLevelObject multiLevel = new MultiLevelObject(
            new Level1(
                new Level2(
                    new Level3("deep", 3)
                )
            )
        );

        String json = JsonUtil.toJSONString(multiLevel);
        MultiLevelObject parsed = JsonUtil.parseObject(json, MultiLevelObject.class);

        assertEquals("deep", parsed.getLevel1().getLevel2().getLevel3().getName());
        assertEquals(3, parsed.getLevel1().getLevel2().getLevel3().getValue());
    }

    /**
     * 测试特殊数据类型的序列化和反序列化
     * 测试场景：
     * 1. BigDecimal 的精度保持
     * 2. Date 类型的处理
     * 3. UUID 的序列化
     * 4. 验证特殊类型的正确性
     */
    @Test
    @DisplayName("测试特殊数据类型序列化和反序列化")
    void testSpecialDataTypes() {
        // 测试特殊数据类型
        SpecialTypesObject special = new SpecialTypesObject(
            new BigDecimal("123.45"),
            new Date(),
            UUID.randomUUID()
        );

        String json = JsonUtil.toJSONString(special);
        SpecialTypesObject parsed = JsonUtil.parseObject(json, SpecialTypesObject.class);

        assertEquals(new BigDecimal("123.45"), parsed.getBigDecimal());
        assertNotNull(parsed.getDate());
        assertNotNull(parsed.getUuid());
    }

    /**
     * 测试循环引用对象的序列化和反序列化
     * 测试场景：
     * 1. 对象之间的循环引用
     * 2. 父子关系的处理
     * 3. 验证循环引用的正确性
     */
    @Test
    @DisplayName("测试循环引用对象序列化和反序列化")
    void testCircularReference() {
        // 测试循环引用
        CircularParentChildObject parent = new CircularParentChildObject("parent");
        CircularParentChildObject child = new CircularParentChildObject("child");
        parent.setChild(child);
        child.setParent(parent);

        String json = JsonUtil.toJSONString(parent);
        CircularParentChildObject parsed = JsonUtil.parseObject(json, CircularParentChildObject.class);

        assertEquals("parent", parsed.getName());
        assertEquals("child", parsed.getChild().getName());

        CircularIdentityObject first = new CircularIdentityObject("first");
        CircularIdentityObject second = new CircularIdentityObject("second");
        CircularIdentityObject third = new CircularIdentityObject("third");
        first.setNext(second);
        second.setNext(third);
        third.setNext(first); // 创建循环引用

        String identityJson = JsonUtil.toJSONString(first);
        CircularIdentityObject identityParsed = JsonUtil.parseObject(identityJson, CircularIdentityObject.class);
        assertEquals("first", identityParsed.getId());
        assertEquals("second", identityParsed.getNext().getId());
        assertEquals("third", identityParsed.getNext().getNext().getId());
        assertEquals("first", identityParsed.getNext().getNext().getNext().getId());
    }

    /**
     * 基本测试对象类
     * 用于测试基本的序列化和反序列化功能
     */
    private static class TestObject {
        private String name;
        private int value;

        public TestObject() {
        }

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    /**
     * 日期时间测试对象类
     * 用于测试日期时间类型的序列化和反序列化
     */
    private static class DateTimeObject {
        private LocalDateTime dateTime;

        public DateTimeObject() {
        }

        public DateTimeObject(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
    }

    /**
     * 基本 Record 类型
     * 用于测试 Record 的基本序列化和反序列化功能
     */
    private record BasicRecord(String name, int value) {}

    /**
     * 嵌套 Record 类型
     * 用于测试 Record 的嵌套序列化和反序列化功能
     */
    private record NestedRecord(BasicRecord basic, String description) {}

    /**
     * 包含 Record 的类
     * 用于测试类中包含 Record 类型的序列化和反序列化
     */
    private static class ClassWithRecord {
        private final BasicRecord record;
        private final String additional;

        public ClassWithRecord() {
            this.record = null;
            this.additional = null;
        }

        public ClassWithRecord(BasicRecord record, String additional) {
            this.record = record;
            this.additional = additional;
        }

        public BasicRecord getRecord() {
            return record;
        }

        public String getAdditional() {
            return additional;
        }
    }

    /**
     * 包含类的 Record
     * 用于测试 Record 中包含类类型的序列化和反序列化
     */
    private record RecordWithClass(TestObject object, String metadata) {}

    /**
     * 复杂集合对象类
     * 用于测试包含多种集合类型的复杂对象的序列化和反序列化
     */
    private static class ComplexCollectionObject {
        private final List<TestObject> list;
        private final Map<String, TestObject> map;
        private final Set<String> set;

        public ComplexCollectionObject() {
            this.list = null;
            this.map = null;
            this.set = null;
        }

        public ComplexCollectionObject(List<TestObject> list, Map<String, TestObject> map, Set<String> set) {
            this.list = list;
            this.map = map;
            this.set = set;
        }

        public List<TestObject> getList() { return list; }
        public Map<String, TestObject> getMap() { return map; }
        public Set<String> getSet() { return set; }
    }

    /**
     * 状态枚举
     * 用于测试枚举类型的序列化和反序列化
     */
    private enum Status {
        ACTIVE, INACTIVE, PENDING
    }

    /**
     * 枚举对象类
     * 用于测试包含枚举类型的对象的序列化和反序列化
     */
    private static class EnumObject {
        private final Status status;
        private final String name;

        public EnumObject() {
            this.status = null;
            this.name = null;
        }

        public EnumObject(Status status, String name) {
            this.status = status;
            this.name = name;
        }

        public Status getStatus() { return status; }
        public String getName() { return name; }
    }

    /**
     * 泛型容器类
     * 用于测试泛型类型的序列化和反序列化
     * @param <T> 容器中存储的数据类型
     */
    private static class GenericContainer<T> {
        private final T data;
        private final int version;

        public GenericContainer() {
            this.data = null;
            this.version = 0;
        }

        public GenericContainer(T data, int version) {
            this.data = data;
            this.version = version;
        }

        public T getData() { return data; }
        public int getVersion() { return version; }
    }

    /**
     * 多层级嵌套对象类
     * 用于测试多层嵌套对象的序列化和反序列化
     */
    private static class MultiLevelObject {
        private final Level1 level1;

        public MultiLevelObject() {
            this.level1 = null;
        }

        public MultiLevelObject(Level1 level1) {
            this.level1 = level1;
        }

        public Level1 getLevel1() { return level1; }
    }

    /**
     * 第一层嵌套类
     * 用于测试多层嵌套结构
     */
    private static class Level1 {
        private final Level2 level2;

        public Level1() {
            this.level2 = null;
        }

        public Level1(Level2 level2) {
            this.level2 = level2;
        }

        public Level2 getLevel2() { return level2; }
    }

    /**
     * 第二层嵌套类
     * 用于测试多层嵌套结构
     */
    private static class Level2 {
        private final Level3 level3;

        public Level2() {
            this.level3 = null;
        }

        public Level2(Level3 level3) {
            this.level3 = level3;
        }

        public Level3 getLevel3() { return level3; }
    }

    /**
     * 第三层嵌套类
     * 用于测试多层嵌套结构
     */
    private static class Level3 {
        private final String name;
        private final int value;

        public Level3() {
            this.name = null;
            this.value = 0;
        }

        public Level3(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    /**
     * 特殊数据类型对象类
     * 用于测试特殊数据类型的序列化和反序列化
     */
    private static class SpecialTypesObject {
        private final BigDecimal bigDecimal;
        private final Date date;
        private final UUID uuid;

        public SpecialTypesObject() {
            this.bigDecimal = null;
            this.date = null;
            this.uuid = null;
        }

        public SpecialTypesObject(BigDecimal bigDecimal, Date date, UUID uuid) {
            this.bigDecimal = bigDecimal;
            this.date = date;
            this.uuid = uuid;
        }

        public BigDecimal getBigDecimal() { return bigDecimal; }
        public Date getDate() { return date; }
        public UUID getUuid() { return uuid; }
    }

    /**
     * 循环引用对象类
     * 用于测试对象间循环引用的序列化和反序列化
     */
    private static class CircularParentChildObject {
        private final String name;
        //通过 @JsonBackReference 和 @JsonManagedReference 注解来处理循环引用
        // 其中 @JsonBackReference 用于标记父对象的引用，@JsonManagedReference 用于标记子对象的引用
        @JsonBackReference
        private CircularParentChildObject parent;
        @JsonManagedReference
        private CircularParentChildObject child;

        public CircularParentChildObject() {
            this.name = null;
            this.parent = null;
            this.child = null;
        }

        public CircularParentChildObject(String name) {
            this.name = name;
        }

        public String getName() { return name; }
        public CircularParentChildObject getParent() { return parent; }
        public void setParent(CircularParentChildObject parent) { this.parent = parent; }
        public CircularParentChildObject getChild() { return child; }
        public void setChild(CircularParentChildObject child) { this.child = child; }
    }

    /**
     * 循环引用对象类，通过 id 标识对象
     * 用于测试循环引用的序列化和反序列化
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private static class CircularIdentityObject {
        private final String id;
        private CircularIdentityObject next;

        public CircularIdentityObject() {
            this.id = null;
        }

        public CircularIdentityObject(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
        public CircularIdentityObject getNext() {
            return next;
        }
        public void setNext(CircularIdentityObject next) {
            this.next = next;
        }
    }
} 