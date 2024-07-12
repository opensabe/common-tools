package io.github.opensabe.common.mybatis.observation;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 统计事务或者SQL执行时间的content
 * @author maheng
 */
@Getter
@Setter
public class SQLExecuteContext extends Observation.Context {
    private final String method;

    private long end;

    private boolean success;

    private final String transactionName;
    public SQLExecuteContext(String method, @Nullable String transactionName) {
        this.method = method;
        this.transactionName = Optional.ofNullable(transactionName).orElse("");
        this.success = true;
    }
}
