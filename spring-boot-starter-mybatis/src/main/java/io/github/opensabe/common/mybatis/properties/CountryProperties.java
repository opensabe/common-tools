package io.github.opensabe.common.mybatis.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

import static io.github.opensabe.common.mybatis.properties.CountryProperties.PREFIX;

@ConfigurationProperties(prefix = PREFIX)
@Getter
@Setter
public class CountryProperties {

    public final static String PREFIX = "country";

    /**
     * key: operId
     * value: country
     */
    private Map<String, String> map;
}
