package io.github.opensabe.common.mybatis.jfr;

import jdk.jfr.*;
import lombok.Getter;
import lombok.Setter;

/**
 * sql执行监控
 * @author maheng
 */
@Getter
@Setter
@Category({"observation","mybatis"})
@Label("SQL Execute Monitor")
@StackTrace(value = false)
public class SQLExecuteEvent extends Event {

    /**
     * 执行sql的方法（mybatis mapper）
     */
    @Label("SQL Executed Method")
    private final String method;

    @Label("Transaction Id")
    private final String transactionName;

    private String traceId;
    private String spanId;

    private final boolean success;

    public SQLExecuteEvent(String method, String transactionName, boolean success) {
        this.method = method;
        this.transactionName = transactionName;
        this.success = success;
    }

}