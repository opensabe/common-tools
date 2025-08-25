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
package io.github.opensabe.common.mybatis.configuration;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

import io.github.opensabe.common.mybatis.interceptor.DataSourceSwitchInterceptor;
import io.github.opensabe.common.mybatis.interceptor.WebMvcDataSourceSwitchInterceptor;
import io.github.opensabe.common.mybatis.interceptor.WebfluxDataSourceSwitchInterceptor;
import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import lombok.extern.log4j.Log4j2;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

/**
 * 根据 request header里的opeId，自动设置数据源
 *
 * @author heng.ma
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class WebInterceptorConfiguration {


    /**
     * mvc
     *
     * @return
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public DataSourceSwitchInterceptor webmvcInterceptor() {
        return new WebMvcDataSourceSwitchInterceptor();
    }


    /**
     * 对webflux支持
     */
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class WebfluxSupportConfiguration {

        @Autowired
        private ApplicationContext applicationContext;

        @Bean
        public WebFilter operatorEventFilter() {
            return (exchange, chain) -> {
                var operId = exchange.getRequest().getHeaders().getFirst("operId");
                if (StringUtils.isNotBlank(operId)) {
                    applicationContext.publishEvent(new OperatorEvent(operId));
                }
                return chain.filter(exchange).doFinally(s -> {
                    Hooks.resetOnEachOperator(HookRefresher.class.getName());
                    DynamicRoutingDataSource.clear();
                });
            };
        }

        @Bean
        public DataSourceSwitchInterceptor webfluxInterceptor() {
            return new WebfluxDataSourceSwitchInterceptor();
        }

        @Bean
        public HookRefresher operatorHookRefresher() {
            return new HookRefresher();
        }


        /**
         * 这里是关键，Mono.subscribeContext的时候，每次都会创建新的context，因此，用Hook，
         * 给context添加operId
         */
        class HookRefresher implements ApplicationListener<OperatorEvent> {

            @Override
            public void onApplicationEvent(OperatorEvent event) {
                var operId = event.getSource();
                Hooks.resetOnEachOperator(HookRefresher.class.getName());
                Hooks.onEachOperator(HookRefresher.class.getName(), Operators.liftPublisher((p, sb) -> {
                    var context = sb.currentContext().put("operId", operId);
                    return new WrappedSubscriber<>(sb, context);
                }));
            }
        }

        class WrappedSubscriber<T> implements CoreSubscriber<T> {


            private CoreSubscriber<T> delegate;
            private Context context;

            public WrappedSubscriber(CoreSubscriber<T> delegate, Context context) {
                this.delegate = delegate;
                this.context = context;
            }

            @Override
            public Context currentContext() {
                return delegate.currentContext().putAll(context);
            }

            @Override
            public void onSubscribe(Subscription s) {
                delegate.onSubscribe(s);
            }

            @Override
            public void onNext(T t) {
                delegate.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                delegate.onError(t);
                delegate.currentContext().delete("operId");
            }

            @Override
            public void onComplete() {
                delegate.onComplete();
                delegate.currentContext().delete("operId");
            }
        }

        class OperatorEvent extends ApplicationEvent {

            /**
             * Create a new {@code ApplicationEvent}.
             *
             * @param source the object on which the event initially occurred or with
             *               which the event is associated (never {@code null})
             */
            public OperatorEvent(Object source) {
                super(source);
            }
        }
    }

}
