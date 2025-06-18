package io.github.opensabe.common.redisson.util;

import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Supplier;

@Log4j2
public class LuaLimitCache {

    private static final long SHOULD_LOAD = -1;

    private static final long REACH_LIMIT = -2;

    //ARGV[1] -> limit
    //ARGV[2] -> increment
    //ARGV[3] -> expire
    private static final String LUA_SCRIPT =
                    "local limit = tonumber(redis.call(\"GET\", KEYS[1]))\n" +
                    "if not limit then\n" +
                    "    return " + SHOULD_LOAD + '\n' +
                    "end\n" +
                    "if limit and limit >= tonumber(ARGV[1]) then\n" +
                    "    return "+ REACH_LIMIT + '\n' +
                    "else\n" +
                    "    local current = tonumber(redis.call(\"INCRBY\", KEYS[1], ARGV[2]))\n" +
                    "    redis.call(\"EXPIRE\", KEYS[1], ARGV[3])\n" +
                    "    return current\n" +
                    "end";

    private static final DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();

    static {
        redisScript.setScriptText(LUA_SCRIPT);
        redisScript.setResultType(Long.class);
    }
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    public LuaLimitCache(StringRedisTemplate redisTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * 是否达到界限
     * @param key redis key
     * @param increment 增长的值
     * @param limit 最大大小
     * @param expire 过期时间
     * @param loader 如果值不存在，加载值，可以保证并发安全
     * @return
     */
    public boolean isReachLimit (
            String key,
            long limit,
            long increment,
            long expire,
            Supplier<Long> loader
    ) {
        long current = redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(limit), String.valueOf(increment), String.valueOf(expire)
        );
        log.info("LuaLimitCache-isReachLimit: {} -> current: {}", key, current);
        if (current == SHOULD_LOAD) {
            RLock lock = redissonClient.getLock(key + ":limit:lock");
            lock.lock();
            try {
                current = redisTemplate.execute(
                        redisScript,
                        Collections.singletonList(key),
                        String.valueOf(limit), String.valueOf(increment), String.valueOf(expire)
                );
                log.info("LuaLimitCache-isReachLimit: grabbed Lock, first check: {} -> current: {}", key, current);
                if (current == SHOULD_LOAD) {
                    redisTemplate.opsForValue().set(key, String.valueOf(loader.get()), Duration.ofSeconds(expire));
                    current = redisTemplate.execute(
                            redisScript,
                            Collections.singletonList(key),
                            String.valueOf(limit), String.valueOf(increment), String.valueOf(expire)
                    );
                    log.info("LuaLimitCache-isReachLimit: grabbed Lock, after fill: {} -> current: {}", key, current);
                }
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        //最后检查是否达到界限
        if (current == REACH_LIMIT) {
            return true;
        }
        return false;
    }

}
