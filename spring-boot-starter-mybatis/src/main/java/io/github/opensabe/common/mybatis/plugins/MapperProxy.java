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
package io.github.opensabe.common.mybatis.plugins;

import io.github.opensabe.common.mybatis.interceptor.CustomizedTransactionInterceptor;
import io.github.opensabe.common.mybatis.observation.SQLExecuteContext;
import io.github.opensabe.common.mybatis.observation.SQLExecuteDocumentation;
import io.github.opensabe.common.mybatis.observation.SQLExecuteObservationConvention;
import io.github.opensabe.common.utils.SpringUtil;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * Mapper代理，跟原生的区别是添加了observation
 * @author maheng
 * @param <T>
 */
public class MapperProxy<T> extends org.apache.ibatis.binding.MapperProxy<T> {
    private final Class<T> mapperInterface;

    private UnifiedObservationFactory observationFactory;
    @SuppressWarnings("unchecked")
    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface,
                       Map methodCache) {
        super(sqlSession, mapperInterface, methodCache);
        this.mapperInterface = mapperInterface;
    }

    public UnifiedObservationFactory getObservationFactory() {
        if (Objects.isNull(observationFactory)  && Objects.nonNull(SpringUtil.getApplicationContext())) {
            observationFactory = SpringUtil.getBean(UnifiedObservationFactory.class);
        }
        return observationFactory;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        UnifiedObservationFactory observationFactory = getObservationFactory();
        if (Objects.isNull(observationFactory) || Objects.isNull(observationFactory.getObservationRegistry())) {
            return super.invoke(proxy, method, args);
        }
        SQLExecuteContext context = new SQLExecuteContext(mapperInterface + "#" + method.getName(), CustomizedTransactionInterceptor.getCurrentTransactionId());
        Observation observation = SQLExecuteDocumentation.SQL_EXECUTE_MAPPER.observation(
                        null,
                        SQLExecuteObservationConvention.DEFAULT,
                        () -> context,
                        observationFactory.getObservationRegistry())
                //即使调用service的地方没有observation,为了让同一个service里的sql处于相同的observation,
                //第一个SQL创建observation以后，设置为第二个parent,这样两个SQL的traceId会保持一致
                .parentObservation(observationFactory.getCurrentObservation())
                .start();
        try {
            return super.invoke(proxy, method, args);
        }catch (Throwable e) {
            observation.error(e);
            context.setSuccess(false);
            throw e;
        }finally {
            observation.stop();
        }
    }
}
