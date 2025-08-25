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
package io.github.opensabe.spring.boot.starter.rocketmq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public enum VersionEnum {
    VERSION_1_0_0("1.0.0");

    private static List<VersionEnum> listOfEnum;
    private static Map<String, VersionEnum> mapOfEnum;

    static {
        List<VersionEnum> tempList = new ArrayList<>();
        Map<String, VersionEnum> tempTemp = new HashMap<>();
        for (VersionEnum item : VersionEnum.values()) {
            tempList.add(item);
            tempTemp.put(item.getValue(), item);
        }
        listOfEnum = List.copyOf(tempList);
        mapOfEnum = Map.copyOf(tempTemp);
    }

    private final String value;

    VersionEnum(String value) {
        this.value = value;
    }

    public static VersionEnum getVersionEnum(String value) {
        VersionEnum item = mapOfEnum.get(value);
        if (item != null) {
            return item;
        }
        return null;
    }
}
