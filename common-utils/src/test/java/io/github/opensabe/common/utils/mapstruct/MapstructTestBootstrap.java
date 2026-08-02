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
package io.github.opensabe.common.utils.mapstruct;

import org.mapstruct.factory.Mappers;

import io.github.opensabe.common.utils.mapstruct.vo.Activity;
import io.github.opensabe.common.utils.mapstruct.vo.ActivityDto;
import io.github.opensabe.common.utils.mapstruct.vo.Customer;
import io.github.opensabe.common.utils.mapstruct.vo.CustomerDto;
import io.github.opensabe.common.utils.mapstruct.vo.Node;
import io.github.opensabe.common.utils.mapstruct.vo.NodeDto;
import io.github.opensabe.common.utils.mapstruct.vo.OrderItemDto;
import io.github.opensabe.common.utils.mapstruct.vo.Person;
import io.github.opensabe.common.utils.mapstruct.vo.PersonRecord;
import io.github.opensabe.common.utils.mapstruct.vo.Types;
import io.github.opensabe.common.utils.mapstruct.vo.TypesDto;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.ActivityActivityDtoMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.ActivityDtoMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.ActivityDtoMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.ActivityMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.ActivityMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.CustomerDtoCustomerMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.CustomerDtoMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.CustomerDtoMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.NodeDtoMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.NodeDtoMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.NodeDtoNodeMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.NodeMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.NodeMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.OrderItemDtoMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.OrderItemDtoMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.PersonMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.PersonMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.PersonPersonRecordMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.TypesMapMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.TypesMapper;
import io.github.opensabe.common.utils.mapstruct.vo.mapper.TypesTypesDtoMapper;
import io.github.opensabe.mapstruct.core.MapperRepository;

/**
 * MapStruct 测试引导类：在 Java 25 下补注册 APT 未完整生成的映射器。
 * <p>
 * 规避 opensabe mapstruct-processor 1.4.0 生成残缺 {@code MapperRegisterImpl} 以及
 * {@link MapperRepository#getInstance()} 每次新建仓库导致注册丢失的问题。
 */
public final class MapstructTestBootstrap {

    private static volatile boolean initialized;

    private MapstructTestBootstrap() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        MapperRepository repo = MapperRepository.getInstance();
        if (repo == null) {
            throw new IllegalStateException("MapperRepository.getInstance() returned null");
        }

        // Prefer CustomerMapper from MapperRegisterImpl; fall back only if missing
        try {
            repo.getMapper(Activity.class, ActivityDto.class);
        }
        catch (RuntimeException e) {
            repo.register(Activity.class, ActivityDto.class, Mappers.getMapper(ActivityActivityDtoMapper.class));
        }

        repo.register(Activity.class, Mappers.getMapper(ActivityMapper.class));
        repo.register(Activity.class, Mappers.getMapper(ActivityMapMapper.class));
        repo.register(ActivityDto.class, Mappers.getMapper(ActivityDtoMapper.class));
        repo.register(ActivityDto.class, Mappers.getMapper(ActivityDtoMapMapper.class));

        repo.register(CustomerDto.class, Customer.class, Mappers.getMapper(CustomerDtoCustomerMapper.class));
        repo.register(CustomerDto.class, Mappers.getMapper(CustomerDtoMapper.class));
        repo.register(CustomerDto.class, Mappers.getMapper(CustomerDtoMapMapper.class));

        repo.register(Node.class, Mappers.getMapper(NodeMapper.class));
        repo.register(Node.class, Mappers.getMapper(NodeMapMapper.class));
        repo.register(NodeDto.class, Mappers.getMapper(NodeDtoMapper.class));
        repo.register(NodeDto.class, Mappers.getMapper(NodeDtoMapMapper.class));
        repo.register(NodeDto.class, Node.class, Mappers.getMapper(NodeDtoNodeMapper.class));

        repo.register(OrderItemDto.class, Mappers.getMapper(OrderItemDtoMapper.class));
        repo.register(OrderItemDto.class, Mappers.getMapper(OrderItemDtoMapMapper.class));

        repo.register(Person.class, Mappers.getMapper(PersonMapper.class));
        repo.register(Person.class, Mappers.getMapper(PersonMapMapper.class));
        repo.register(Person.class, PersonRecord.class, Mappers.getMapper(PersonPersonRecordMapper.class));

        repo.register(Types.class, Mappers.getMapper(TypesMapper.class));
        repo.register(Types.class, Mappers.getMapper(TypesMapMapper.class));
        repo.register(Types.class, TypesDto.class, Mappers.getMapper(TypesTypesDtoMapper.class));

        initialized = true;
    }
}
