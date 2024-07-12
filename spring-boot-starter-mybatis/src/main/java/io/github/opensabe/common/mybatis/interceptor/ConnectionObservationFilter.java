package io.github.opensabe.common.mybatis.interceptor;

import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.stat.JdbcConnectionStat;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.common.mybatis.observation.ConnectionContext;
import io.github.opensabe.common.mybatis.observation.ConnectionDocumentation;
import io.github.opensabe.common.mybatis.observation.ConnectionObservationConvention;
import io.github.opensabe.common.utils.SpringUtil;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Objects;

/**
 * 监控连接池获取和释放
 * @author maheng
 */
public class ConnectionObservationFilter extends FilterAdapter {

    private DataSourceProxy dataSourceProxy;

    /**
     * 缓存等待锁的线程数量
     */
    private Cache<String, Integer> WAIT_THREAD_COUNT_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(1))
            .build();

    private UnifiedObservationFactory observationFactory;

    @Override
    public void init(DataSourceProxy dataSource) {
        super.init(dataSource);
        this.dataSourceProxy =  dataSource;
    }

    public UnifiedObservationFactory getObservationFactory() {
        if (Objects.isNull(observationFactory) && Objects.nonNull(SpringUtil.getApplicationContext())) {
            observationFactory = SpringUtil.getBean(UnifiedObservationFactory.class);
        }
        return observationFactory;
    }


    /**
     * 获取连接
     * @param chain
     * @param dataSource
     * @param maxWaitMillis
     * @return
     * @throws SQLException
     */
    @Override
    public DruidPooledConnection dataSource_getConnection(FilterChain chain, DruidDataSource dataSource, long maxWaitMillis) throws SQLException {
        UnifiedObservationFactory unifiedObservationFactory = getObservationFactory();
        if (Objects.isNull(unifiedObservationFactory)  || Objects.isNull(unifiedObservationFactory.getObservationRegistry())) {
            return super.dataSource_getConnection(chain, dataSource, maxWaitMillis);
        }
        ConnectionContext context = ConnectionContext.connect(
                dataSource.getMaxWaitThreadCount(),
                maxWaitMillis,
                dataSource.getMaxActive());
        Observation observation = ConnectionDocumentation.CONNECT
                .observation(null,
                        ConnectionObservationConvention.DEFAULT,
                        () -> context,
                        unifiedObservationFactory.getObservationRegistry())
                .start();
        try {
            DruidPooledConnection result =  super.dataSource_getConnection(chain, dataSource, maxWaitMillis);
            context.setActiveCount(dataSource.getDataSourceStat().getConnections().size());
            context.setConnectedTime(result.getConnectedTimeMillis());
            context.setWaitThread(WAIT_THREAD_COUNT_CACHE.get("c", key -> dataSource.getLockQueueLength()));
            return result;
        }catch (Throwable e) {
            context.setSuccess(false);
            context.setWaitThread(dataSource.getLockQueueLength());
            observation.error(e);
            throw e;
        }finally {
            observation.stop();
        }

    }

    /**
     * 连接回收
     * @param chain
     * @param connection
     * @throws SQLException
     */
    @Override
    public void dataSource_releaseConnection(FilterChain chain, DruidPooledConnection connection) throws SQLException {
        UnifiedObservationFactory unifiedObservationFactory = getObservationFactory();
        if (Objects.isNull(unifiedObservationFactory) || Objects.isNull(unifiedObservationFactory.getObservationRegistry())) {
            super.dataSource_releaseConnection(chain, connection);
            return;
        }
        JdbcConnectionStat connectionStat = dataSourceProxy.getDataSourceStat().getConnectionStat();
        ConnectionContext context = ConnectionContext.release(
                connectionStat.getActiveMax(),
                connectionStat.getActiveCount(),
                connection.getConnectedTimeMillis()
                );
        Observation observation = ConnectionDocumentation.RELEASE.observation(null,
                ConnectionObservationConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            super.dataSource_releaseConnection(chain, connection);
            context.setActiveCount(connectionStat.getActiveCount());
        }catch (Throwable e) {
            context.setSuccess(false);
            observation.error(e);
            throw e;
        }finally {
            observation.stop();
        }
    }

}
