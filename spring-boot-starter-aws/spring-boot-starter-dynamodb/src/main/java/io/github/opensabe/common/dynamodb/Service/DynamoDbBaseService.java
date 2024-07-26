package io.github.opensabe.common.dynamodb.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.RangeKeyName;
import io.github.opensabe.common.dynamodb.annotation.TableName;
import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteContext;
import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteDocumentation;
import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteObservationConvention;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.micrometer.observation.Observation;
import it.unimi.dsi.fastutil.Pair;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

@Log4j2
public abstract class DynamoDbBaseService<T> {

    @Autowired
    public DynamoDbClient dynamoDbClient;
    @Value("${aws_env}")
    public String environment;
    @Value("${defaultOperId:0}")
    public String defaultOperId;
    @Autowired
    public UnifiedObservationFactory unifiedObservationFactory;
    public static final String PLACE_HOLDER = "${aws_env}";
    public static final String OPER_HOLDER = "${defaultOperId}";
    private static final Cache<Class, List<Field>> classFieldsCache = Caffeine.newBuilder().maximumSize(1024).build();
    private static final Cache<String, Method> methodCache = Caffeine.newBuilder().maximumSize(1024).build();

    public String getRealTableName(T t) {
        return getRealTableName(t.getClass());
    }

    public String getRealTableName(Class clazz) {
        TableName table = (TableName) clazz.getDeclaredAnnotation(TableName.class);
        String tableName = table.name();
        String realTableName = tableName.replace(PLACE_HOLDER, environment);
        realTableName = realTableName.replace(OPER_HOLDER, defaultOperId);
        return realTableName;
    }

    /**
     * 有且只能包含用@HashKeyname或@RangekeyName标识字段值，其它字段查询不生效
     *
     * @param t
     * @return
     */
    public T selectOne(T t) {
        long startTime = System.currentTimeMillis();
        String tableName = getRealTableName(t);

        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName + "#" + "selectOne");
        Observation observation = DynamodbExecuteDocumentation.SQL_EXECUTE_SELECT
                .observation(null,
                        DynamodbExecuteObservationConvention.DEFAULT,
                        () -> context,
                        unifiedObservationFactory.getObservationRegistry())
                .parentObservation(unifiedObservationFactory.getCurrentObservation())
                .start();

        String jsonParam = JsonUtil.toJSONString(t);
        log.info("select table:{},param:{}", tableName, jsonParam);
        Class<T> clazz = (Class<T>) t.getClass();
        T object = null;
        try {
            GetItemRequest request = buildGetItemRequest(tableName,t, context);
            if (Objects.nonNull(request)) {
                Map<String, AttributeValue> returnedItem = dynamoDbClient.getItem(request).item();
                if (returnedItem != null) {
                    object = combineFieldsToObject(returnedItem, clazz);
                }
            }
            log.info("selectByItem cost {}ms", (System.currentTimeMillis() - startTime) / 1000);
        } catch (Throwable throwable) {
            log.error("DynamoDbBaseService-selectOne {} ", throwable.getMessage(), throwable);
            observation.error(throwable);
        } finally {
            observation.stop();
        }
        return object;
    }

    /**
     * 有且只能包含用@HashKeyname或@RangekeyName标识字段值,其它字段查询不生效
     *
     * @param t
     * @return
     */
    public List<T> selectList(T t) {
        long startTime = System.currentTimeMillis();
        String tableName = getRealTableName(t);
        String jsonParam = JsonUtil.toJSONString(t);
        log.info("select table:{},param:{}", tableName, jsonParam);

        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName + "#" + "selectList");
        Observation observation = DynamodbExecuteDocumentation.SQL_EXECUTE_SELECT.observation(null, DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry()).parentObservation(unifiedObservationFactory.getCurrentObservation()).start();

        List<T> list = Lists.newArrayList();
        try {
            QueryRequest queryReq = buildQueryRequest(t, context);
            if (Objects.nonNull(queryReq)) {
                QueryResponse response = dynamoDbClient.query(queryReq);

                if (response.count() > 0) {
                    response.items().forEach(stringAttributeValueMap -> {
                        T object = combineFieldsToObject(stringAttributeValueMap, (Class<T>) t.getClass());
                        list.add(object);
                    });
                }
            }
        } catch (Throwable e) {
            log.error("DynamoDBQueryService-selectByRequest:{}", e.getMessage(), e);
            observation.error(e);
        } finally {
            log.info("DynamoDBQueryService-selectByRequest cost {}ms", (System.currentTimeMillis() - startTime) / 1000);
            observation.stop();
        }
        return list;
    }

    public void save(T t) {
        String tableName = getRealTableName(t);
        DynamodbExecuteContext context = new DynamodbExecuteContext(tableName + "#" + "save");
        Observation observation = DynamodbExecuteDocumentation.SQL_EXECUTE_INSERT.observation(null, DynamodbExecuteObservationConvention.DEFAULT, () -> context, unifiedObservationFactory.getObservationRegistry()).parentObservation(unifiedObservationFactory.getCurrentObservation()).start();
        try {
            String jsonParam = JsonUtil.toJSONString(t);
            log.info("save table:{},param:{}", tableName, jsonParam);
            //封装数据
            Map<String, AttributeValue> item = buildAttributeValues(t, context);
            //数据保存
            dynamoDbClient.putItem(PutItemRequest.builder().tableName(tableName).item(item).build());
        } catch (Throwable e) {
            log.error("save failed param:{}", t.toString(), e);
            observation.error(e);
        } finally {
            observation.stop();
        }
    }

    public T combineFieldsToObject(Map<String, AttributeValue> returnedItem, Class<T> clazz) {
        Map<String, Object> map = new HashMap<>(returnedItem.keySet().size());
        Set<String> keys = returnedItem.keySet();

        for (String key : keys) {
            if (returnedItem.get(key).n() != null) {
                map.put(key, returnedItem.get(key).n());
            } else if (returnedItem.get(key).bool() != null) {
                map.put(key, returnedItem.get(key).bool());
            } else {
                map.put(key, returnedItem.get(key).s());
            }
        }
        T object = JSON.parseObject(JsonUtil.toJSONString(map), clazz);
        return object;
    }

    public GetItemRequest buildGetItemRequest(String tableName,T t, DynamodbExecuteContext context) throws InvocationTargetException, IllegalAccessException {
        Map<String, AttributeValue> map = Maps.newHashMap();
        GetItemRequest getItemRequest = null;
        Class<T> clazz = (Class<T>) t.getClass();
        List<Field> fields = getClassFieldsFromCache(clazz);
        for (Field field : fields) {
            Method method = getMethodFromCache(field.getName(), clazz);
            Object value = method != null ? method.invoke(t) : null;
            if (value != null) {
                Pair<String, AttributeValue> pair = buildAttributeValue(t, field);
                if (Objects.nonNull(pair)) {

                    String hashFieldName = pair.key();
                    AttributeValue attributeValue = pair.value();

                    HashKeyName hashKeyName = field.getAnnotation(HashKeyName.class);
                    RangeKeyName rangeKeyName = field.getAnnotation(RangeKeyName.class);

                    if (Objects.nonNull(attributeValue)) {
                        boolean isHashKey = Objects.nonNull(hashKeyName);
                        boolean isRangeKey = Objects.nonNull(rangeKeyName);
                        if (isHashKey || isRangeKey) {
                            map.put(hashFieldName, attributeValue);
                            if (isHashKey) {
                                context.setHashKey(attributeValue.toString());
                            } else if (isRangeKey) {
                                context.setRangeKey(attributeValue.toString());
                            }
                        }
                    }
                }
            }
        }
        if (map.isEmpty()) {
            log.error("DynamoDBQueryService-buildGetItemRequest,hashkey or rangekey can't null {}", t.toString());
        } else {
            getItemRequest = GetItemRequest.builder().key(map).consistentRead(true).tableName(tableName).build();
        }
        return getItemRequest;
    }

    public QueryRequest buildQueryRequest(T t, DynamodbExecuteContext context) throws InvocationTargetException, IllegalAccessException {
        HashMap<String, String> attrNameAlias = Maps.newHashMap();
        HashMap<String, AttributeValue> attrValues = Maps.newHashMap();
        StringBuilder conditionExpression = new StringBuilder();
        Class<T> clazz = (Class<T>) t.getClass();
        List<Field> fields = getClassFieldsFromCache(clazz);

        for (Field field : fields) {
            Method method = getMethodFromCache(field.getName(), clazz);
            Object value = method != null ? method.invoke(t) : null;
            if (value != null) {
                Pair<String, AttributeValue> pair = buildAttributeValue(t, field);
                if (Objects.nonNull(pair)) {
                    String hashFieldName = pair.key();
                    AttributeValue attributeValue = pair.value();
                    HashKeyName hashKeyName = field.getAnnotation(HashKeyName.class);
                    RangeKeyName rangeKeyName = field.getAnnotation(RangeKeyName.class);
                    if (Objects.nonNull(attributeValue)) {
                        boolean isHashKey = Objects.nonNull(hashKeyName);
                        boolean isRangeKey = Objects.nonNull(rangeKeyName);
                        if (isHashKey || isRangeKey) {
                            String hashValue = value + "";
                            String hashKeyAlias = "#" + field.getName();
                            if (!StringUtils.isEmpty(hashValue)) {
                                attrNameAlias.put(hashKeyAlias, hashFieldName);
                                attrValues.put(":" + hashFieldName, attributeValue);
                                if (conditionExpression.length() > 0) {
                                    conditionExpression.append(" And " + hashKeyAlias + " = :" + hashFieldName);
                                } else {
                                    conditionExpression.append(hashKeyAlias + " = :" + hashFieldName);
                                }
                                String string = attributeValue.toString();
                                if (isHashKey) {
                                    context.setHashKey(string);
                                } else if (isRangeKey) {
                                    context.setRangeKey(string);
                                }
                            }
                        }
                    }
                }

            }
        }
        if (StringUtils.isBlank(conditionExpression.toString())) {
            log.error("DynamoDBQueryService-buildQueryRequest:conditionExpression can't empty {}", t.toString());
            return null;
        } else {
            return QueryRequest.builder().tableName(getRealTableName(clazz)).keyConditionExpression(conditionExpression.toString())
//                .filterExpression(StringUtils.isBlank(filterExpression.toString()) ? null : filterExpression.toString())
                    .expressionAttributeNames(attrNameAlias).expressionAttributeValues(attrValues).build();
        }
    }


    public Map<String, AttributeValue> buildAttributeValues(T t, DynamodbExecuteContext context) {
        Map<String, AttributeValue> items = new HashMap<String, AttributeValue>();
        Class<T> clazz = (Class<T>) t.getClass();
        List<Field> fields = getClassFieldsFromCache(clazz);
        for (Field field : fields) {
            boolean isHashKey = Objects.nonNull(field.getAnnotation(HashKeyName.class));
            boolean isRangeKey = Objects.nonNull(field.getAnnotation(RangeKeyName.class));
            Pair<String, AttributeValue> pair = buildAttributeValue(t, field);
            if (Objects.nonNull(pair)) {
                items.put(pair.key(), pair.value());
                if (isHashKey) {
                    context.setHashKey(pair.value().toString());
                } else if (isRangeKey) {
                    context.setRangeKey(pair.value().toString());
                }
            }
        }
        return items;
    }

    public Pair<String, AttributeValue> buildAttributeValue(T t, Field field) {

        Class<T> clazz = (Class<T>) t.getClass();

        String hashFieldName;
        JSONField jsonField = field.getAnnotation(JSONField.class);
        if (Objects.isNull(jsonField)) {
            hashFieldName = field.getName();
        } else {
            hashFieldName = jsonField.name();
        }
        try {
            Method method = getMethodFromCache(field.getName(), clazz);
            Object value = method != null ? method.invoke(t) : null;
            if (Objects.nonNull(value)) {
                if (value instanceof String) {
                    return Pair.of(hashFieldName, AttributeValue.builder().s(value.toString()).build());
                } else if (value instanceof Number) {
                    return Pair.of(hashFieldName, AttributeValue.builder().n(value.toString()).build());
                } else if (value instanceof Boolean) {
                    return Pair.of(hashFieldName, AttributeValue.builder().bool(((Boolean) value)).build());
                } else if (value instanceof Date) {
                    return Pair.of(hashFieldName, AttributeValue.builder().s(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(((Date) value).getTime())).build());
                } else {
                    log.error("BaseService-buildAttributeValue:Field type mismatch {}", field.getType());
                }
            }
        } catch (IllegalAccessException e) {
            log.error("BaseService-buildAttributeValue:{}", e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error("BaseService-buildAttributeValue:{}", e.getMessage(), e);
        } catch (DynamoDbException e) {
            log.error("BaseService-buildAttributeValue:{}", e.getMessage(), e);
        }
        return null;
    }

    private List<Field> getClassFieldsFromCache(Class className) {
        return classFieldsCache.get(className, (key) -> {
            log.info("DynamoDbBaseService-getClassFields invoke {} ", className);
            return List.of(key.getDeclaredFields());
        });
    }

    public Method getMethodFromCache(String fieldName, Class clazz) {
        String keyName = clazz.getName() + "#" + fieldName;
        return methodCache.get(keyName, key -> {
            try {
                log.info("DynamoDbBaseService-getMethod invoke {} ", keyName);
                PropertyDescriptor property = new PropertyDescriptor(fieldName, clazz);
                return property.getReadMethod();
            } catch (Throwable throwable) {
                log.error("DynamoDbBaseService-getMethod failed {} {}", keyName, throwable.getMessage(), throwable);
            }
            return null;
        });
    }
}
