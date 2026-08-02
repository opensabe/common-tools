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

import java.util.HashMap;
import java.util.Map;

/**
 * 测试用 {@code MapperRepositoryImpl} 替代实现。
 * <p>
 * {@link MapperRepository#getInstance()} 每次新建实例会导致实例字段映射丢失注册；
 * 静态 Map 保证 {@link io.github.opensabe.common.utils.mapstruct.MapstructTestBootstrap} 的注册在 Java 25 下可见。
 */
public class MapperRepositoryImpl implements MapperRepository {

	private static final Map<FromToKey, CommonCopyMapper> COMMON_MAPPER = new HashMap<>();
	private static final Map<Class, FromMapMapper> MAP_MAPPER = new HashMap<>();
	private static final Map<Class, SelfCopyMapper> SELF_MAPPER = new HashMap<>();

	public MapperRepositoryImpl() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S, T> CommonCopyMapper<S, T> getMapper(Class<S> source, Class<T> target) {
		CommonCopyMapper mapper = null;
		if (source == target) {
			mapper = SELF_MAPPER.getOrDefault(source, SELF_MAPPER.get(target));
		}
		else {
			mapper = COMMON_MAPPER.get(new FromToKey(source, target));
			if (mapper == null) {
				CommonCopyMapper<T, S> reverse = COMMON_MAPPER.get(new FromToKey(target, source));
				if (reverse != null) {
					mapper = new ReverseMapper<>(reverse);
					COMMON_MAPPER.put(new FromToKey(source, target), mapper);
				}
			}
		}
		if (mapper == null) {
			throw new MapperNotFoundException(source, target);
		}
		return (CommonCopyMapper<S, T>) mapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> FromMapMapper<T> getMapMapper(Class<T> target) {
		var mapper = MAP_MAPPER.get(target);
		if (mapper == null) {
			throw new MapperNotFoundException(target);
		}
		return (FromMapMapper<T>) mapper;
	}

	@Override
	public <S, T> void register(Class<S> source, Class<T> target, CommonCopyMapper<S, T> mapper) {
		COMMON_MAPPER.put(new FromToKey(source, target), mapper);
	}

	@Override
	public <T> void register(Class<T> target, FromMapMapper<T> mapper) {
		MAP_MAPPER.put(target, mapper);
	}

	@Override
	public <T> void register(Class<T> target, SelfCopyMapper<T> mapper) {
		SELF_MAPPER.put(target, mapper);
	}
}
