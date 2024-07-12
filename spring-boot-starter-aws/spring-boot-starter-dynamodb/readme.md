# spring-boot-starter-dynamodb 使用文档

**引入依赖**：
```
<groupId>io.github.opensabe-tech</groupId>
<artifactId>spring-boot-starter-dynamodb</artifactId>
<version>去 maven 看最新版本</version>
```

```
配置 aws dynamodb 地址：
```
aws_access_key_id: ${dynamo_access_key}

aws_secret_access_key: ${dynamo_secret_access_key}

aws_region: eu-central-1

aws_env: online
```

## 使用介绍

定义表实体，加入dynamodb 映射字段
```
```
@Data
@NoArgsConstructor
//表名
@TableName(name = "dynamodb_${aws_env}_eight_data_types")
@Data
public class EightDataTypesPo {

    @JSONField(name =  "id")
    //映射 dynamodb对应表的 hashkey
    @HashKeyName(name="id")
    private String id;
    
    @JSONField(name = "order")
    ////映射 dynamodb对应表的 rangeKey
    @RangeKeyName(name="order")
    private Integer order;
    
    @JSONField(name = "it")
    private int num1;

}
```
继承抽象类 DynamoDbBaseService(该类提供了访问dynamodb的方法) 就可以使用：
```
@Service
public class EightDataTypesManager extends DynamoDbBaseService<EightDataTypesPo> {

}
```

### observation

#### dynamodb 保存,dynamodb 查询
```
事件名称：aws.execute.dynamodb.insert,aws.execute.dynamodb.select

触发时机：调用保存方法
```
##### HighCardinalityKeyValues

| 属性  |类型| 备注                                                   |
| ------------ | ------------ |------------------------------------------------------|
| hashKey   | string  | dynamodb 表指定 hashKey(类似主键)                           |
| method  | string | 执行方法名                                                |
| rangeKey  | string | dynamodb 表 范围键                                       |


##### LowCardinalityKeyValues
```
同 HighCardinalityKeyValues
```

##### JFR
```
事件名称：Dynamodb Execute Monitor

事件分类（所属文件夹）：observation.Dynamodb-Execute
```
|属性|备注|
| ------------ | ------------ |
|hashKey|dynamodb 表指定 hashKey(类似主键) |
|method|执行方法名|
|rangeKey|dynamodb 表 范围键|
```



