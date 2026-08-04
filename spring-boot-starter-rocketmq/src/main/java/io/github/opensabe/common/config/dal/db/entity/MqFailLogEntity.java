/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.config.dal.db.entity;

/**
 * MqFailLogEntity。
 */
public class MqFailLogEntity {
/** id。 */
    private String id;

/** topic。 */
    private String topic;

/** hashKey。 */
    private String hashKey;

/** traceId。 */
    private String traceId;

/** sendConfig。 */
    private String sendConfig;

/** retryNum。 */
    private Integer retryNum;

/** sendStatus。 */
    private Integer sendStatus;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SUCCESS = 1;

/** body。 */
    private String body;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id 待设置值
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic 待设置值
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * @return hashKey
     */
    public String getHashKey() {
        return hashKey;
    }

    /**
     * @param hashKey 待设置值
     */
    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    /**
     * @return traceId
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * @param traceId 待设置值
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * @return sendConfig
     */
    public String getSendConfig() {
        return sendConfig;
    }

    /**
     * @param sendConfig 待设置值
     */
    public void setSendConfig(String sendConfig) {
        this.sendConfig = sendConfig;
    }

    /**
     * @return retryNum
     */
    public Integer getRetryNum() {
        return retryNum;
    }

    /**
     * @param retryNum 待设置值
     */
    public void setRetryNum(Integer retryNum) {
        this.retryNum = retryNum;
    }

    /**
     * @return sendStatus
     */
    public Integer getSendStatus() {
        return sendStatus;
    }

    /**
     * @param sendStatus 待设置值
     */
    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    /**
     * @return body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body 待设置值
     */
    public void setBody(String body) {
        this.body = body;
    }
}