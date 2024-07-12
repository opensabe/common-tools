package io.github.opensabe.common.idgenerator.service;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public interface UniqueID {
    /**
     * @param bizType 业务类型编号
     * @return id     全局唯一id
     */
    String getUniqueId(String bizType);

    String getLongUniqueId(String bizType);

    String getUniqueIdWithTimeOut(String bizType, long time, TimeUnit timeUnit) throws Exception;

    /**
     * 适用于每毫秒不超过 1000 个业务
     *
     * @param bizType
     * @return
     */
    String getShortUniqueId(String bizType);
}
