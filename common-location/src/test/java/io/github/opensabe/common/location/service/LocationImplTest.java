package io.github.opensabe.common.location.service;

import io.github.opensabe.common.location.service.GeoLocation;
import io.github.opensabe.common.location.service.IpToLocation;
import io.github.opensabe.common.location.service.WorldCityService;
import io.github.opensabe.common.location.vo.GeoLocationData;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "defaultOperId=2"
})
public class LocationImplTest {


    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> redisServer = new FixedHostPortGenericContainer<>("redis")
            .withFixedExposedPort(6379,6379)
            .withExposedPorts(6379)
            .withCommand("redis-server");


    @BeforeAll
    public static void setUp() {
        System.out.println("start redis");
        redisServer.start();
        System.out.println("redis started");
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("stop redis");
        redisServer.stop();
        System.out.println("redis stopped");
    }

    @EnableAutoConfiguration
    @Configuration
    public static class App {
    }

    @Autowired
    private IpToLocation ipToLocation;
    @Autowired
    private GeoLocation geoLocation;
    @Autowired
    private WorldCityService worldCityService;

    @Test
    public void testGetRegion()  {
        Assertions.assertEquals(ipToLocation.getNearest("86.23.52.41"), ipToLocation.getNearest("86.23.52.41"));
    }

    @Test
    public void testGetNearest()  {
        GeoLocationData nearest = geoLocation.getNearest(37.42301, -122.083352);
        System.out.println(nearest);
    }
}

