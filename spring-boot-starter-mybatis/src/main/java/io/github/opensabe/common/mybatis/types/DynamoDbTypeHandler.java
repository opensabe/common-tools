package io.github.opensabe.common.mybatis.types;

import io.github.opensabe.common.typehandler.OBSTypeEnum;

/**
 * <p>保存JSON字符串时，将大JSON保存到DynamoDB,然后把相应的key保存到数据库</p>
 *
 * <p>
 *  查询时，先在数据库查出key,然后再根据key从DynamoDB获取到JSON,最后由
 *  JSONTypeHandler转换成对象
 * </p>
 * @see JSONTypeHandler
 * @author rushuangwang
 */
public class DynamoDbTypeHandler extends OBSTypeHandler{

    public DynamoDbTypeHandler(Class<?> type) {
        super(type);
    }
    @Override
    protected OBSTypeEnum type() {
        return OBSTypeEnum.DYNAMODB;
    }
}
