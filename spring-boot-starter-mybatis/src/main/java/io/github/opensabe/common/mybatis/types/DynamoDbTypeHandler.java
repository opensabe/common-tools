/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.mybatis.types;

import io.github.opensabe.common.typehandler.OBSTypeEnum;

/**
 * <p>保存JSON字符串时，将大JSON保存到DynamoDB,然后把相应的key保存到数据库</p>
 *
 * <p>
 * 查询时，先在数据库查出key,然后再根据key从DynamoDB获取到JSON,最后由
 * JSONTypeHandler转换成对象
 * </p>
 *
 * @author rushuangwang
 * @see JSONTypeHandler
 */
public class DynamoDbTypeHandler extends OBSTypeHandler {

    public DynamoDbTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    protected OBSTypeEnum type() {
        return OBSTypeEnum.DYNAMODB;
    }
}
