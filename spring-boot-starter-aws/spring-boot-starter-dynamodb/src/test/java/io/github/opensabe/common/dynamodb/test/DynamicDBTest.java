package io.github.opensabe.common.dynamodb.test;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteContext;
import io.github.opensabe.common.dynamodb.test.common.DynamicdbStarter;
import io.github.opensabe.common.dynamodb.test.common.EightDataTypesManager;
import io.github.opensabe.common.dynamodb.test.po.EightDataTypesPo;
import io.github.opensabe.common.dynamodb.typehandler.DynamoDbOBService;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.typehandler.OBSTypeEnum;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@Import(DynamicDBTest.LogContentHandler.class)
public class DynamicDBTest extends DynamicdbStarter {

    @Autowired
    private EightDataTypesManager eightDataTypesManager;
    @Autowired
    private DynamoDbOBService dynamoDbOBService;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;


    @Configuration(proxyBeanMethods = false)
    public static class LogContentHandler implements ObservationHandler<DynamodbExecuteContext> {

        @Override
        public void onStop(DynamodbExecuteContext context) {
            System.out.println(context);
        }

        @Override
        public boolean supportsContext(Observation.Context context) {
            return context instanceof DynamodbExecuteContext;
        }
    }


    @Test
    void testEightDataTypesPo() {
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
    void testdynamoDbOBService() {
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

    @Test
    void testUpdate () {
        EightDataTypesPo save = save("333");
        Assertions.assertEquals(222, save.getOrder());
        Assertions.assertEquals(111, save.getNum1());

        save.setOrder(333);
        save.setNum1(999);

        eightDataTypesManager.save(save);

        EightDataTypesPo po = eightDataTypesManager.selectOne(save);

        Assertions.assertEquals(333, po.getOrder());
        Assertions.assertEquals(999, po.getNum1());
    }

    @Test
    void testDelete () {
        EightDataTypesPo save = save("333");
        Assertions.assertEquals(222, save.getOrder());
        Assertions.assertEquals(111, save.getNum1());;

        eightDataTypesManager.deleteByKey(save);

        EightDataTypesPo po = eightDataTypesManager.selectOne(save);

        Assertions.assertNull(po);
    }

    @Test
    void testQueryList () {
        save("444");
        List<EightDataTypesPo> pos = eightDataTypesManager.selectList(QueryConditional.sortGreaterThanOrEqualTo(Key.builder()
                .partitionValue("444").sortValue(111).build()));
        assertEquals(1, pos.size());
        List<EightDataTypesPo> pos1 = eightDataTypesManager.selectList(QueryConditional.sortGreaterThanOrEqualTo(Key.builder()
                .partitionValue("444").sortValue(333).build()));
        assertEquals(0, pos1.size());
        List<EightDataTypesPo> pos2 = eightDataTypesManager.selectList(QueryConditional.sortGreaterThanOrEqualTo(Key.builder()
                .partitionValue("555").sortValue(111).build()));
        assertEquals(0, pos2.size());
    }
}
