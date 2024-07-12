package io.github.opensabe.common.mybatis.base;

import com.alibaba.ttl.TransmittableThreadLocal;
import tk.mybatis.mapper.entity.IDynamicTableName;

public abstract class ArchivedTable implements IDynamicTableName {
    private static TransmittableThreadLocal<Boolean> currentIsHistory = new TransmittableThreadLocal<>();

    public static void setIsHistory(boolean isHistory) {
        currentIsHistory.set(isHistory);
    }

    protected abstract String tableName();

    protected abstract String historyTableName();
    
    @Override
    public String getDynamicTableName() {
        Boolean aBoolean = currentIsHistory.get();
        currentIsHistory.remove();
        if (aBoolean != null && aBoolean) {
            return historyTableName();
        } else {
            return tableName();
        }
    }
}
