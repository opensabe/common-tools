package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.mapper.user.DynamodbTypeHandlerMapper;
import io.github.opensabe.common.mybatis.test.po.DynamodbPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.sql.Connection;
import java.sql.Statement;

public class DynamodbTypeHandlerTest extends BaseDataSourceTest{
    @Autowired
    private DynamodbTypeHandlerMapper dynamodbTypeHandlerMapper;
    @Autowired
    private DynamoDbClient dynamoDbClient;
    @Value("${defaultOperId:2}")
    private String defaultOperId;
    @Value("${aws_env:test}")
    private String aws_env;
    @BeforeEach
    public void before () {
        dynamicRoutingDataSource.getResolvedDataSources().values().forEach(dataSource -> {
            try (
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()
            ) {
                statement.execute("create table if not exists t_dynamodb_type_handler(id varchar(64) primary key, order_info varchar(1280));");
                statement.execute("delete from t_dynamodb_type_handler;");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        dynamoDbClient.createTable(builder ->
                builder.tableName("dynamodb_"+aws_env+"_"+defaultOperId+"_typehandler")
                        .provisionedThroughput((p) -> {
                            p.readCapacityUnits(500l);
                            p.writeCapacityUnits(500l);
                        })
                        .keySchema(
                                KeySchemaElement.builder().attributeName("key").keyType(KeyType.HASH).build()
                        )
                        .attributeDefinitions(
                                AttributeDefinition.builder().attributeName("key").attributeType(ScalarAttributeType.S).build()
                        ));
    }

    @Test
    public void testDynamodyTypeHandler () {
//        dynamodbTypeHandlerMapper.deleteByPrimaryKey("order1");
        var dynamodbPO = new DynamodbPO();
        dynamodbPO.setId("order1");
        var info = new DynamodbPO.OrderInfo();
        info.setMarket(1);
        info.setMatchId("222222");
        dynamodbPO.setOrderInfo(info);
        dynamodbTypeHandlerMapper.insertSelective(dynamodbPO);
        var db = dynamodbTypeHandlerMapper.selectByPrimaryKey("order1");
        System.out.println(db.getOrderInfo().getMatchId());
    }

}
