package io.github.opensabe.common.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.opensabe.common.utils.json.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = JsonUtilTest.App.class)
public class JsonUtilTest {

    @SpringBootApplication
    public static class App {

    }



    public record User(String name, Integer age, LocalDateTime brithDay) {
    }

    @Test
    void testSerialize () {
        System.out.println(JsonUtil.toJSONString(new User("lily", 10, LocalDateTime.now())));
    }

    @Test
    void toJSONBytesTest() {
        Map<String, String> data = new HashMap<>();
        data.put("id", "Test\\String");
        System.out.println(Arrays.toString(JsonUtil.toJSONBytes(data)));
        // 下面是与fastjson的toJSONBytes比较，返回true
        // System.out.println(Arrays.toString(JsonUtil.toJSONBytes(data)).equals(Arrays.toString(JSON.toJSONBytes(data))));
    }

    @Test
    void testDeserialize () {
        String src = """
                {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"}
                """;
        System.out.println(JsonUtil.parseObject(src, User.class));
    }
    @Test
    void testDeserializeTypeReference () {
        String src = """
                {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"}
                """;
        System.out.println(JsonUtil.parseObject(src, new TypeReference<User>() {
        }));
    }
    @Test
    void testDeserializeTypeReferenceByteArray () {
        String src = """
                {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"}
                """;
        System.out.println(JsonUtil.parseObject(src.getBytes(StandardCharsets.UTF_8), new TypeReference<User>() {
        }));
    }
    @Test
    void testDeserializeByteArray () {
        String src = """
                {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"}
                """;
        System.out.println(JsonUtil.parseObject(src.getBytes(StandardCharsets.UTF_8), User.class));
    }

    @Test
    void testDeserializeArray () {
        String src = """
                [
                    {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"},
                    {"name": "luc1", "age":11, "brithDay": "2024-06-06T11:10:00Z"},
                    {"name": "luc2", "age":12, "brithDay": "2024-06-06T11:10:00Z"}
                ]
                """;
        User[] users = JsonUtil.parseArray(src, User.class);
        Arrays.stream(users).forEach(System.out::println);
        assertEquals(3, users.length);
    }
    @Test
    void testDeserializeArrayByteArray () {
        String src = """
                [
                    {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"},
                    {"name": "luc1", "age":11, "brithDay": "2024-06-06T11:10:00Z"},
                    {"name": "luc2", "age":12, "brithDay": "2024-06-06T11:10:00Z"}
                ]
                """;
        User[] users = JsonUtil.parseArray(src.getBytes(StandardCharsets.UTF_8), User.class);
        Arrays.stream(users).forEach(System.out::println);
        assertEquals(3, users.length);
    }
    @Test
    void testDeserializeList () {
        String src = """
                [
                    {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"},
                    {"name": "luc1", "age":11, "brithDay": "2024-06-06T11:10:00Z"},
                    {"name": "luc2", "age":12, "brithDay": "2024-06-06T11:10:00Z"}
                ]
                """;
        List<User> users = JsonUtil.parseList(src, User.class);
        System.out.println(users);
        assertEquals(3, users.size());
    }
    @Test
    void testDeserializeListByteArray () {
        String src = """
                [
                    {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"},
                    {"name": "luc1", "age":11, "brithDay": "2024-06-06T11:10:00Z"},
                    {"name": "luc2", "age":12, "brithDay": "2024-06-06T11:10:00Z"}
                ]
                """;
        List<User> users = JsonUtil.parseList(src.getBytes(StandardCharsets.UTF_8), User.class);
        System.out.println(users);
        assertEquals(3, users.size());
    }
    @Test
    void testDeserializeMap () {
        String src = """
                   {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"}
                """;
        Map<String, Object> map = JsonUtil.parseMap(src, Object.class);
        System.out.println(map);
        assertEquals(3, map.size());
    }
    @Test
    void testDeserializeMapByteArray () {
        String src = """
                   {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"}
                """;
        Map<String, Object> map = JsonUtil.parseMap(src.getBytes(StandardCharsets.UTF_8), Object.class);
        System.out.println(map);
        assertEquals(3, map.size());
    }

    @Test
    void testJsonNode () {
        String src = """
                {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"}
                """;
        JsonNode jsonNode = JsonUtil.parseObject(src);
        System.out.println(jsonNode);
        assertEquals("luc",jsonNode.findValue("name").asText());
        assertEquals(10,jsonNode.findValue("age").asInt());
        assertEquals("2024-06-06T11:10:00Z",jsonNode.findValue("brithDay").asText());
    }
    @Test
    void testJsonNodeByteArray () {
        String src = """
                {"name": "luc", "age":10, "brithDay": "2024-06-06T11:10:00Z"}
                """;
        JsonNode jsonNode = JsonUtil.parseObject(src.getBytes(StandardCharsets.UTF_8));
        System.out.println(jsonNode);
        assertEquals("luc",jsonNode.findValue("name").asText());
        assertEquals(10,jsonNode.findValue("age").asInt());
        assertEquals("2024-06-06T11:10:00Z",jsonNode.findValue("brithDay").asText());
    }

    @Test
    void testLocalDateTimeDeserialize () {
        LocalDateTime now = LocalDateTime.now();

        String s1 = getLocalDateTimeMapStr(now, "yyyy-MM-dd HH:mm:ss");
        Map<String, LocalDateTime> m1 = JsonUtil.parseMap(s1, LocalDateTime.class);
        System.out.println("-------------" +m1.get("brithDay"));
        assertEquals(now.truncatedTo(ChronoUnit.SECONDS), m1.get("brithDay"));

        String s2 = getLocalDateTimeMapStr(now, "yyyy-MM-dd HH:mm:ss.SSS");
        Map<String, LocalDateTime> m2 = JsonUtil.parseMap(s2, LocalDateTime.class);
        System.out.println("-------------" +m2.get("brithDay"));
        assertEquals(now.truncatedTo(ChronoUnit.MILLIS), m2.get("brithDay"));

        String s3 = getLocalDateTimeMapStr(now, "yyyy-MM-dd");
        Map<String, LocalDateTime> m3 = JsonUtil.parseMap(s3, LocalDateTime.class);
        System.out.println("-------------" +m3.get("brithDay"));
        assertEquals(now.truncatedTo(ChronoUnit.DAYS), m3.get("brithDay"));

        String s4 = getLocalDateTimeMapStr(now, "yyyyMMdd");
        Map<String, LocalDateTime> m4 = JsonUtil.parseMap(s4, LocalDateTime.class);
        System.out.println("-------------" +m4.get("brithDay"));
        assertEquals(now.truncatedTo(ChronoUnit.DAYS), m4.get("brithDay"));

        String s5 = getLocalDateTimeMapStr(now, "yyyyMMddHHmmss");
        Map<String, LocalDateTime> m5 = JsonUtil.parseMap(s5, LocalDateTime.class);
        System.out.println("-------------" +m5.get("brithDay"));
        assertEquals(now.truncatedTo(ChronoUnit.SECONDS), m5.get("brithDay"));


    }

    private String getLocalDateTimeMapStr (LocalDateTime now, String format) {
        return """
                {"brithDay": "%s"}
                """.formatted(now.format(DateTimeFormatter.ofPattern(format)));
    }

    /**
     * 测试fastjson序列化：
     * 针对fastjson 1.2.83  以及兼容的2.0.51 版本结果
     */
    @Test
    void testFastjsonSerialize () {
        // fastjson(1.2.83)  序列化结果：{}  不支持record
        // fastjson(2.0.51)  序列化结果：{"age":10,"brithDay":1722396158549,"name":"lily"}   支持record
        // JsonUtil  序列化结果：{"name":"lily","age":10,"brithDay":1722396388723}           支持record
        System.out.println(JSON.toJSONString(new User("lily", 10, LocalDateTime.now())));
    }
}
