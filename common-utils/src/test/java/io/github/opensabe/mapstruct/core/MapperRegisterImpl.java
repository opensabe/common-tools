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
package io.github.opensabe.mapstruct.core;

import org.mapstruct.factory.Mappers;

import io.github.opensabe.common.utils.mapstruct.CustomerMapper;
import io.github.opensabe.common.utils.mapstruct.vo.Activity;
import io.github.opensabe.common.utils.mapstruct.vo.ActivityDto;

/**
 * 测试用 {@code MapperRegisterImpl} 替代实现。
 * <p>
 * 规避 Java 25 下 opensabe mapstruct-processor 1.4.0 生成残缺注册代码的问题；
 * 其余 {@code @Binding} 映射器由 {@link io.github.opensabe.common.utils.mapstruct.MapstructTestBootstrap} 注册。
 */
public class MapperRegisterImpl extends MapperRegister {

	public MapperRegisterImpl(MapperRepository repository) {
		super(repository);
	}

	@Override
	public void register() {
		register(Activity.class, ActivityDto.class, Mappers.getMapper(CustomerMapper.class));
	}
}
