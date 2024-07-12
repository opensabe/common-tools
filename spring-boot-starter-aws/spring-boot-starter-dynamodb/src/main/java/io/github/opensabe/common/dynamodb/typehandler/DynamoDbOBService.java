package io.github.opensabe.common.dynamodb.typehandler;

import io.github.opensabe.common.dynamodb.Service.KeyValueDynamoDbService;
import io.github.opensabe.common.typehandler.OBSService;
import io.github.opensabe.common.typehandler.OBSTypeEnum;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Log4j2
public class DynamoDbOBService implements OBSService {

    private KeyValueDynamoDbService dynamoDbUpdateService;

    public DynamoDbOBService(KeyValueDynamoDbService dynamoDbBaseService){
        this.dynamoDbUpdateService = dynamoDbBaseService;
    }

    @Override
    public OBSTypeEnum type() {
        return OBSTypeEnum.DYNAMODB;
    }

    @Override
    public void insert(String key, String json) {
        KeyValueDynamoDbService.KeyValueMap map = new KeyValueDynamoDbService.KeyValueMap();

        map.setKey(key);
        map.setValue(json);
        dynamoDbUpdateService.save(map);
    }

    @Override
    public String select(String key) {
        if(Objects.isNull(key)
                || !StringUtils.containsIgnoreCase(key, OBSTypeEnum.DYNAMODB.getIdShortName())
                || key.length() > 30) {
            log.warn("DynamoDbOBService.select [discovery illegality key] please check {}", key);
            return key;
        }
        KeyValueDynamoDbService.KeyValueMap map = new KeyValueDynamoDbService.KeyValueMap();
        map.setKey(key);
        KeyValueDynamoDbService.KeyValueMap result = dynamoDbUpdateService.selectOne(map);
        if (Objects.nonNull(result)) {
            return result.getValue();
        }
        return null;
    }
}
