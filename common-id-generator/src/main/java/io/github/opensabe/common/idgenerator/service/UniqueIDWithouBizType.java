package io.github.opensabe.common.idgenerator.service;

import java.util.concurrent.TimeUnit;

public interface UniqueIDWithouBizType {
    /**
     * @param bizType 业务类型编号
     * @return id     全局唯一id
     */
    Long getUniqueId(String bizType);

    Long getUniqueIdWithTimeOut(String bizType, long time, TimeUnit timeUnit) throws Exception;

    /**
     * 适用于每毫秒不超过 1000 个业务
     *
     * @param bizType
     * @return
     */
    Long getShortUniqueId(String bizType);
}
