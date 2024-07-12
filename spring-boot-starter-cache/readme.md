# spring-boot-starter-cache 使用文档

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-boot-starter-cache</artifactId>
<version>去 maven 看最新版本</version>
```

### 1. 配置：

当项目需要Spring Cahce - Redis的时候，需要配置 [spring-boot-starter-redisson](..%2Fspring-boot-starter-redisson) 需要的所有数据库相关的配置。
在此基础上，增加 Spring Cache 配置:
```
caches:
  # 是否启用本项目包
  enabled: true
  custom:
  	#第一個緩存
  	#缓存名称
    - cacheNames: caffeineCache
	  #缓存类型 - Caffeine
      type: caffeine
	  #缓存描述
	  cacheDesc: "For store XXXX"
	  #缓存Propertie名称 应该与type相同
      caffeine:
	    #缓存设置
        spec: maximumSize=50000,expireAfterWrite=10s,cacheNullValues=true
	  #第二个缓存
    - cacheNames: redisCache
	  #当项目用到Spring Cache - Redis, 就需要配置spring-boot-starter-redisson里面要的配置。
	  #缓存类型 - Redis
      type: redis
      redis:
        timeToLive: 1m
```

#### 1.1 Caffeine 设置说明
| 参数| 类型| 描述|
| ------------ | ------------ | ------------ |
| initialCapacity  | Integer  | 初始的缓存空间大小  |
| maximumSize  | Long  | 缓存的最大条数  |
| maximumWeight  | Long  | 缓存的最大权重  |
| expireAfterAccess  | Duration  | 最后一次写入或访问后，指定经过多长的时间过期  |
| expireAfterWrite  | Duration  | 最后一次写入后，指定经过多长的时间缓存过期  |
| refreshAfterWrite  | Duration  | 创建缓存或者最近一次更新缓存后，经过指定的时间间隔后刷新缓存 (需要重写CacheLoader)  |
| weakKeys  | Boolean  | 打开 key 的弱引用  |
| weakValues  | Boolean  | 打开 value 的弱引用  |
| softValues  | Boolean  | 打开 value 的软引用  |
| cacheNullValues  | Boolean | 是否允许缓存Null Value [默认：true] |

#### 1.2 Redis 设置说明
| 参数| 类型| 描述|
| ------------ | ------------ | ------------ |
| timeToLive  | Duration  | 指定经过多长的时间缓存过期  |
| cacheNullValues  | Boolean | 是否允许缓存Null Value [默认：true] |
| keyPrefix  | String  | 前綴  |

### 2. 使用 Spring Cache注解：
对于某个方法使用 @Cacheable:
```
# 将方法的结果缓存起来，下一次方法执行参数相同时，将不执行方法，返回缓存中的结果
# value等于配置里的cacheNames
@Cacheable(value = "caffeineCache", key = "#id")
public ItemObject getItemFromCaffeine(Long id) {
	return storage.getItem(id);
}

# 如果不指定Key 将会默认使用Method Param作为Key值 以下的Key值会为 id
@Cacheable(value = "caffeineCache")
public ItemObject getItemFromCaffeine(Long id) {
	return storage.getItem(id);
}

# 如果Method 有多于一个的Param 将会集合所有Param为Key值 以下的Key值会为 id:itemType
@Cacheable(value = "caffeineCache")
public ItemObject getItemFromCaffeine(Long id, String itemType) {
	return storage.getItem(id, itemType);
}

# 如果不指定Key 和 Method也没有Param 最后将会取Method名为Key值 以下的Key值会为 getItemFromCaffeine
@Cacheable(value = "caffeineCache")
public ItemObject getItemFromCaffeine() {
	return storage.getAllItem();
}
```

对于某个方法使用 @CachePut:
```
# 方法总会执行，根据注解的配置将结果缓存
@CachePut(value = "redisCache", key = "'redis:'+#id")
public String updateItemFromRedis(Long id) {
	return storage.getItem(id).getName();
}
```


对于某个方法使用 @CacheEvict:
```
# 移除指定缓存
@CacheEvict(value = "redisCache", key = "'redis:'+#id")
public void deleteItemFromRedis(Long id) {
	storage.deleteItem(id);
}

#或

#移除所有缓存
@CacheEvict(value = "caffeineCache")
public void deleteAllItemFromCaffeine() {}
```

特别注意，当使用的缓存类型为Redis时，并**不能使用**@CacheEvict移除所有缓存
因为生产环境上的 Redis 禁止了 Keys 命令， 所以没有指定Key的情况下，一定会报错

### 3. 其他用法：

#### 3.1 直接操作缓存
```
@Autowired
private CacheManager cacheManager;

CaffeineCache cache = (CaffeineCache) cacheManager.getCache("caffeineCache");
RedisCache cache = (RedisCache) cacheManager.getCache("redisCache");

#...
```

#### 3.2 重写 CacheLoader
当项目的Caffeine缓存需要用到**refreshAfterWrite**这个设置时
可以在项目下使用以下的方法来针对特定的Caffeine缓存自定CacheLoader
```
@Configuration
public class CacheConfiguration {

	#必须和YAML所设置的cacheNames同名
	#Key，Value 可以换成其他对象，不一定要Object
	@Bean("caffeineCache")
    public CacheLoader<Object, Object> cacheLoader(XxxService xxxService) {
        return key -> xxxService.selectById(key).getxxx();
    }
}
```

**注意**：当Caffeine缓存需要自定CacheLoader时，设置上的cacheNames只能写一个，不然对不上

### 4. Actuator 接口：

**注意**：官方的Actuator接口 /actuator/cache**s** (っ●ω●)っ [我们自己的没有 s ] 因为有些东西不兼容 最好不要用

#### 4.1 查询微服务内的所有缓存信息
```
接口：/actuator/cache - [GET Request]

Response:
{
    "cacheType": null,    #Always Be NULL
    "cacheName": null,    #Always Be NULL
    "success": true,      #Always Be TRUE
    "message": null,      #Always Be NULL
    "data": [             #缓存信息
        {
            "settings": {
                "spec": "maximumSize=50000,expireAfterWrite=10s,cacheNullValues=true"
            },
            "cacheDesc": "For store XXXX",
            "type": "CAFFEINE",
            "cacheNames": [
                "caffeineCache"
            ]
        },
        {
            "settings": {
                "timeToLive": "PT1M",
                "cacheNullValues": true,
                "keyPrefix": null,
                "useKeyPrefix": true,
                "enableStatistics": false
            },
            "cacheDesc": null,
            "type": "REDIS",
            "cacheNames": [
                "redisCache"
            ]
        }
    ]
}
```

#### 4.2 查询一个缓存内的所有 Key
```
接口：/actuator/cache/{cacheName}/{pageSize}/{pageNum} - [GET Request]
例子：/actuator/cache/caffeineCache/10/1

Response:
{
    "cacheType": "CaffeineCache",        #缓存类型
    "cacheName": "caffeineCache",        #缓存名称 
    "success": true,                     #接口结果
    "message": "Size: 1",                #当Success为True时，返回 缓存内的Key Size。
										 #False时，返回错误信息
	
    "data": [                            # Set of Keys
        "220307175339num56664309"
    ]
}
```
**注意：** pageSize 和 pageNum必须大于0

#### 4.3 查询一个缓存内指定Key的值
```
接口：/actuator/cache/{cacheName}/{key} - [GET Request]
例子：/actuator/cache/caffeineCache/220307175339num56664309

Response:
{
    "cacheType": "CaffeineCache",        
    "cacheName": "caffeineCache",        
    "success": true,                     
    "message": null,                #当Success为True时，返回 NULL。False时，返回错误信息
    "data": [                       # Key值
        "XXXXX"
    ]
}
```


#### 4.4 删除一个缓存的某个 Key
```
接口：/actuator/cache/{cacheName}/{key} - [DELETE Request]
例子：/actuator/cache/redisCache/220307175339num56664309

Response:
{
    "cacheType": "RedisCache",
    "cacheName": "redisCache",
    "success": true,
    "message": null,          #当Success为True时，返回 NULL。False时，返回错误信息
    "data": [                 #接口输入的Key值
        "220307175339num56664309"
    ]
}
```

#### 4.5 清空一个缓存(支持Redis & Caffeine 缓存)
```
接口：/actuator/cache/{cacheName} - [DELETE Request]
例子：/actuator/cache/caffeineCache

Response:
{
    "cacheType": "CaffeineCache",
    "cacheName": "caffeineCache",
    "success": true,
    "message": null,              #当Success为True时，返回 NULL。False时，返回错误信息
    "data": []                    #Always Be NULL
}
```