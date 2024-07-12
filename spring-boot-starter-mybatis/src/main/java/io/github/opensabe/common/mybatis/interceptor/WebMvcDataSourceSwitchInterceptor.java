package io.github.opensabe.common.mybatis.interceptor;

import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import io.github.opensabe.common.mybatis.properties.CountryProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Intercepts({
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }),
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
@Log4j2
public class WebMvcDataSourceSwitchInterceptor extends DataSourceSwitchInterceptor {


    public WebMvcDataSourceSwitchInterceptor() {
//        super( defaultOperId, countryProperties);
    }

    @Override
    public void configureDataSourceContext(BoundSql boundSql) {
        var request = getRequest();
        var operId = "";
        if (request != null) {
            operId = request.getHeader("operId");
        }
        DynamicRoutingDataSource.setDataSourceCountryCode(getCurrentOperCode(operId));
        if (boundSql != null
                && StringUtils.containsIgnoreCase(boundSql.getSql().replace(" ", ""), "/*#mode=readonly*/")) {
            DynamicRoutingDataSource.setDataSourceRW("read");
        } else if (StringUtils.isBlank(DynamicRoutingDataSource.getDataSourceRW())){
            DynamicRoutingDataSource.setDataSourceRW("write");
        }
    }

    private static HttpServletRequest getRequest() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                return servletRequestAttributes.getRequest();
            } else {
                return null;
            }
        } catch (Throwable e) {
            return null;
        }
    }
}
