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
package io.github.opensabe.spring.boot.starter.rocketmq.autoconf;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 目前官方rocket.spring.boot.starter没有升级到spring6.x
 * 自动启动用的是spring.factories，到了spring 6.x就不支持这种启动方式
 * 等官方升级到Spring boot 3可以去掉该类
 */
@AutoConfiguration(before = RocketMQAutoConfiguration.class)
@Import(org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration.class)
public class RocketMQAutoConfigSpring6Support {
}
