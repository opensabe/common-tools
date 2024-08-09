package io.github.opensabe.common.dynamodb.test;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.dynamodb.test.po.EightDataTypesPo;
import io.github.opensabe.common.dynamodb.typehandler.DynamoDbOBService;
import io.github.opensabe.common.typehandler.OBSTypeEnum;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class DynamicDBTest extends DynamicdbStarter {


    private EightDataTypesManager eightDataTypesManager;
    @Autowired
    private DynamoDbOBService dynamoDbOBService;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Autowired
    public DynamicDBTest(EightDataTypesManager eightDataTypesManager) {
        this.eightDataTypesManager = eightDataTypesManager;
    }


    @Test
    public void testEightDataTypesPo() {
        unifiedObservationFactory.getCurrentOrCreateEmptyObservation().observe(() -> {
            EightDataTypesPo save1 = save("id1");
            EightDataTypesPo save2 = save("id1");
            query(save1, save2);
        });
    }

    private void query(EightDataTypesPo params1, EightDataTypesPo params2) {
        EightDataTypesPo eightDataTypesPo = new EightDataTypesPo();
        eightDataTypesPo.setId(params1.getId());
        eightDataTypesPo.setOrder(params1.getOrder());
        EightDataTypesPo result = eightDataTypesManager.selectOne(eightDataTypesPo);
        log.info("DynamicDBTest-test result {}", JsonUtil.toJSONString(result));
        assertEquals(params1, result);
        eightDataTypesPo = new EightDataTypesPo();
        eightDataTypesPo.setOrder(params1.getOrder());
        eightDataTypesManager.selectList(eightDataTypesPo).forEach(eightDataTypesPo1 -> {
            params1.setId(eightDataTypesPo1.getId());
            assertEquals(params1, eightDataTypesPo1);
        });
    }

    private EightDataTypesPo save(String id) {
        EightDataTypesPo eightDataTypesPo = new EightDataTypesPo();
        eightDataTypesPo.setId(id);
        eightDataTypesPo.setOrder(222);
        eightDataTypesPo.setNum1(111);
        eightDataTypesPo.setDb1(2.2);
        eightDataTypesPo.setFt1(222.2f);
        eightDataTypesPo.setFlag1(Boolean.TRUE);
        eightDataTypesManager.save(eightDataTypesPo);
        return eightDataTypesPo;
    }

    @Test
    public void testdynamoDbOBService() {
        EightDataTypesPo eightDataTypesPo = new EightDataTypesPo();
        eightDataTypesPo.setId("id3");
        eightDataTypesPo.setOrder(222);
        eightDataTypesPo.setNum1(111);
        eightDataTypesPo.setDb1(2.2);
        eightDataTypesPo.setFt1(222.2f);
        eightDataTypesPo.setFlag1(Boolean.TRUE);
        dynamoDbOBService.insert(OBSTypeEnum.DYNAMODB.getIdShortName() + "key1", JSON.toJSONString(eightDataTypesPo));
        String key1 = dynamoDbOBService.select(OBSTypeEnum.DYNAMODB.getIdShortName() + "key1");
        EightDataTypesPo eightDataTypesPo1 = JSON.parseObject(key1, EightDataTypesPo.class);
        assertEquals(eightDataTypesPo, eightDataTypesPo1);
    }
}
