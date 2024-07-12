package io.github.opensabe.common.mybatis.autoconf;

import io.github.opensabe.common.mybatis.configuration.MonitorConfiguration;
import io.github.opensabe.common.mybatis.configuration.MybatisConfiguration;
import io.github.opensabe.common.mybatis.configuration.PageHelperProperties;
import io.github.opensabe.common.mybatis.configuration.SqlSessionFactoryConfiguration;
import io.github.opensabe.common.mybatis.properties.CountryProperties;
import io.github.opensabe.common.mybatis.configuration.PageHelperAutoConfiguration;
import io.github.opensabe.common.mybatis.configuration.WebInterceptorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import tk.mybatis.mapper.autoconfigure.MybatisProperties;

@AutoConfiguration
@Import({
        MybatisConfiguration.class,
        PageHelperAutoConfiguration.class,
        SqlSessionFactoryConfiguration.class,
        WebInterceptorConfiguration.class,
        MonitorConfiguration.class
})
@EnableConfigurationProperties({MybatisProperties.class, PageHelperProperties.class, CountryProperties.class})
public class MyBatisAutoConfiguration {
}
