# Spring-boot-starter-elasticsearch 使用文档

由于ES对于版本比较敏感，而且spring-data-elasticsearch对于咱们的需求搜索权重设计不太友好，所以编写这个模块，直接暴露 RestHighLevelClient

## 使用说明

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-boot-starter-elasticsearch</artifactId>
<version>去 maven 看最新版本</version>
```

在 application.yml 添加配置：
```
#这里必须填写elasticsearch的http接口的地址而不是transport端口的地址，transport端口即将废弃
spring:
  data:
    elasticsearch:
      addresses: 172.31.64.194:9200,172.31.64.193:9200
```
获取RestHighLevelClient
```
@Autowired
private RestHighLevelClient client;
```

client参考文档：

1. [Get Started](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-getting-started-initialization.html)
2. [JavaDocs](https://artifacts.elastic.co/javadoc/org/elasticsearch/client/elasticsearch-rest-high-level-client/7.0.1/index.html)

## Observation

针对所有请求，增加了 Observation

### HighCardinalityKeyValues

| 属性  | 类型     | 备注     |
|-----|--------|--------|
| elastic.search.client.request.uri | string | 请求 uri |
| elastic.search.client.request.params | string | 请求参数 |
| elastic.search.client.response | string | 返回结果 |
| elastic.search.client.throwable | string | 异常信息 |



### LowCardinalityKeyValues
无，都不适合
