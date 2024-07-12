package io.github.opensabe.spring.boot.starter.rocketmq;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum VersionEnum {
    VERSION_1_0_0("1.0.0")
    ;
    VersionEnum(String value) {
        this.value = value;
    }
    private final String value;

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

    public static VersionEnum getVersionEnum(String value) {
        VersionEnum item = mapOfEnum.get(value);
        if (item != null) {
            return item;
        }
        return null;
    }
}
