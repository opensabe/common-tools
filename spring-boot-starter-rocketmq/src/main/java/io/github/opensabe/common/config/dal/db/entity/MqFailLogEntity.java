package io.github.opensabe.common.config.dal.db.entity;

import java.util.Date;

public class MqFailLogEntity {
    private String id;

    private String topic;

    private String hashKey;

    private String traceId;

    private String sendConfig;

    private Integer retryNum;

    private Integer sendStatus;

    private Date createTime;

    private Date updateTime;

    private String body;

    public MqFailLogEntity(String id, String topic, String hashKey, String traceId, String sendConfig, Integer retryNum, Integer sendStatus, Date createTime, Date updateTime) {
        this.id = id;
        this.topic = topic;
        this.hashKey = hashKey;
        this.traceId = traceId;
        this.sendConfig = sendConfig;
        this.retryNum = retryNum;
        this.sendStatus = sendStatus;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public MqFailLogEntity(String id, String topic, String hashKey, String traceId, String sendConfig, Integer retryNum, Integer sendStatus, Date createTime, Date updateTime, String body) {
        this.id = id;
        this.topic = topic;
        this.hashKey = hashKey;
        this.traceId = traceId;
        this.sendConfig = sendConfig;
        this.retryNum = retryNum;
        this.sendStatus = sendStatus;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.body = body;
    }

    @Deprecated
    public MqFailLogEntity(String test) {
        this.body = test;
    }

    public MqFailLogEntity() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic == null ? null : topic.trim();
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey == null ? null : hashKey.trim();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId == null ? null : traceId.trim();
    }

    public String getSendConfig() {
        return sendConfig;
    }

    public void setSendConfig(String sendConfig) {
        this.sendConfig = sendConfig == null ? null : sendConfig.trim();
    }

    public Integer getRetryNum() {
        return retryNum;
    }

    public void setRetryNum(Integer retryNum) {
        this.retryNum = retryNum;
    }

    public Integer getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body == null ? null : body.trim();
    }
}