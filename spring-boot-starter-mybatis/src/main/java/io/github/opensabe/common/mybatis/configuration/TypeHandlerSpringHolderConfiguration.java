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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.typehandler.OBSService;
import io.github.opensabe.common.typehandler.OBSTypeEnum;
import io.github.opensabe.common.utils.SpringUtil;

/**
 * 因为OBSTypeHandler每次都需要创建新实例，将需要用的的bean保存到静态变量中提高效率
 *
 * @author hengma
 */
public class TypeHandlerSpringHolderConfiguration {

    private static final Map<OBSTypeEnum, OBSService> OBS_SERVICES = new HashMap<>();
    private static UniqueID uniqueID;

    public static OBSService getService(OBSTypeEnum type) {
        if (OBS_SERVICES.containsKey(type)) {
            return OBS_SERVICES.get(type);
        }
        var service = SpringUtil.getApplicationContext().getBeanProvider(OBSService.class)
                .stream().filter(s -> Objects.equals(type, s.type()))
                .findFirst()
                .orElse(null);
        OBS_SERVICES.put(type, service);
        return service;
    }

    public static UniqueID getUniqueID() {
        if (Objects.isNull(uniqueID)) {
            uniqueID = SpringUtil.getBean(UniqueID.class);
        }
        return uniqueID;
    }
}
