package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.observation.ObservedRedissonClient;
import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * 检查组件是否生效
 * @author heng.ma
 */
public class RedisComponentTest extends BaseRedissonTest {

    private final RedissonClient redissonClient;
    private final RedisConnectionFactory redisConnectionFactory;

    @Autowired
    public RedisComponentTest(RedissonClient redissonClient, RedisConnectionFactory redisConnectionFactory) {
        this.redissonClient = redissonClient;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * 因为JFR单元测试默认没有打开，如果RedissonClientBeanPostProcessor没有生效不能及时检查到
     * 这个单元测试是为了确保RedissonClientBeanPostProcessor中的RedissonClient被替换为ObservedRedissonClient
     */
    @Test
    void testRedissonClient() {
        Assertions.assertInstanceOf(ObservedRedissonClient.class, redissonClient);
    }

    /**
     * 这个单元测试是为了确保RedissonAutoConfigurationV2中的连接池没有覆盖掉LettuceConnectionFactory的配置
     */
    @Test
    void testRedisConnectionFactory() {
        Assertions.assertInstanceOf(LettuceConnectionFactory.class, redisConnectionFactory);
    }

}
