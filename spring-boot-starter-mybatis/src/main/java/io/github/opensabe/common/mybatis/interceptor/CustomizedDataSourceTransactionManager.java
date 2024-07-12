package io.github.opensabe.common.mybatis.interceptor;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;
import java.io.EOFException;

@Log4j2
public class CustomizedDataSourceTransactionManager extends DataSourceTransactionManager {
    public CustomizedDataSourceTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 在 aurora 数据库异常重启，升级，主从切换的时候，遇到的。客户端发了 commit 的包，但是不知道服务端是否收到，连接就关闭了。
     * 会有类似于下面的报错：
     * Caused by: java.io.EOFException: Can not read response from server. Expected to read 4 bytes, read 0 bytes before connection was unexpectedly lost.
     *      at com.mysql.cj.protocol.FullReadInputStream.readFully(FullReadInputStream.java:67) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.SimplePacketReader.readHeaderLocal(SimplePacketReader.java:81) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.SimplePacketReader.readHeader(SimplePacketReader.java:63) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.SimplePacketReader.readHeader(SimplePacketReader.java:45) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.TimeTrackingPacketReader.readHeader(TimeTrackingPacketReader.java:52) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.TimeTrackingPacketReader.readHeader(TimeTrackingPacketReader.java:41) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.MultiPacketReader.readHeader(MultiPacketReader.java:54) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.MultiPacketReader.readHeader(MultiPacketReader.java:44) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.NativeProtocol.readMessage(NativeProtocol.java:514) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.NativeProtocol.checkErrorMessage(NativeProtocol.java:700) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.NativeProtocol.sendCommand(NativeProtocol.java:639) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.NativeProtocol.sendQueryPacket(NativeProtocol.java:987) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.protocol.a.NativeProtocol.sendQueryString(NativeProtocol.java:933) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.NativeSession.execSQL(NativeSession.java:664) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.mysql.cj.jdbc.ConnectionImpl.commit(ConnectionImpl.java:795) ~[mysql-connector-java-8.0.28.jar!/:8.0.28]
     *      at com.alibaba.druid.filter.FilterChainImpl.connection_commit(FilterChainImpl.java:199) ~[druid-1.2.6.jar!/:1.2.6]
     *      at com.alibaba.druid.filter.stat.StatFilter.connection_commit(StatFilter.java:276) ~[druid-1.2.6.jar!/:1.2.6]
     *      at com.alibaba.druid.filter.FilterChainImpl.connection_commit(FilterChainImpl.java:194) ~[druid-1.2.6.jar!/:1.2.6]
     *      at com.alibaba.druid.filter.FilterAdapter.connection_commit(FilterAdapter.java:782) ~[druid-1.2.6.jar!/:1.2.6]
     *      at com.alibaba.druid.filter.logging.LogFilter.connection_commit(LogFilter.java:434) ~[druid-1.2.6.jar!/:1.2.6]
     *      at com.alibaba.druid.filter.FilterChainImpl.connection_commit(FilterChainImpl.java:194) ~[druid-1.2.6.jar!/:1.2.6]
     *      at com.alibaba.druid.proxy.jdbc.ConnectionProxyImpl.commit(ConnectionProxyImpl.java:122) ~[druid-1.2.6.jar!/:1.2.6]
     *      at com.alibaba.druid.pool.DruidPooledConnection.commit(DruidPooledConnection.java:771) ~[druid-1.2.6.jar!/:1.2.6]
     *      at org.springframework.jdbc.datasource.DataSourceTransactionManager.doCommit(DataSourceTransactionManager.java:333) ~[spring-jdbc-5.3.18.jar!/:5.3.18]
     *      ... 110 more
     * 这时候我们不知道究竟 Aurora 是否收到了，如果收到了，事务就提交了。没收到事务就会回滚
     * 目前没啥太好的办法去解决，但是出现的概率不大，就先在事务管理器加上报警，
     * 大家注意查询 traceID 相关的日志然后去数据库查询事务是否提交以及是否需要手动修改数据，给用户补偿什么的。
     * @param status the status representation of the transaction
     */
    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        try {
            super.doCommit(status);
        } catch (TransactionSystemException e) {
            Throwable rootCause = NestedExceptionUtils.getRootCause(e);
            if (rootCause instanceof EOFException) {
                log.fatal("UNIQUE !!!!!!!!!!!!!!!!!!!!!!WARNING!!!!!!!!!!!!!!!!!!!! " +
                        "Not sure whether current commit is sent to DB, manually check is required. " +
                        "Please follow the traceId and find the related info required to check if transaction is committed " +
                        "or if there is something to modify");
            }
            throw e;
        }
    }
}
