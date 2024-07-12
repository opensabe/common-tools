package io.github.opensabe.common.config.dal.db.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MqFailLogEntityExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MqFailLogEntityExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andTopicIsNull() {
            addCriterion("topic is null");
            return (Criteria) this;
        }

        public Criteria andTopicIsNotNull() {
            addCriterion("topic is not null");
            return (Criteria) this;
        }

        public Criteria andTopicEqualTo(String value) {
            addCriterion("topic =", value, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicNotEqualTo(String value) {
            addCriterion("topic <>", value, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicGreaterThan(String value) {
            addCriterion("topic >", value, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicGreaterThanOrEqualTo(String value) {
            addCriterion("topic >=", value, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicLessThan(String value) {
            addCriterion("topic <", value, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicLessThanOrEqualTo(String value) {
            addCriterion("topic <=", value, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicLike(String value) {
            addCriterion("topic like", value, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicNotLike(String value) {
            addCriterion("topic not like", value, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicIn(List<String> values) {
            addCriterion("topic in", values, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicNotIn(List<String> values) {
            addCriterion("topic not in", values, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicBetween(String value1, String value2) {
            addCriterion("topic between", value1, value2, "topic");
            return (Criteria) this;
        }

        public Criteria andTopicNotBetween(String value1, String value2) {
            addCriterion("topic not between", value1, value2, "topic");
            return (Criteria) this;
        }

        public Criteria andHashKeyIsNull() {
            addCriterion("hash_key is null");
            return (Criteria) this;
        }

        public Criteria andHashKeyIsNotNull() {
            addCriterion("hash_key is not null");
            return (Criteria) this;
        }

        public Criteria andHashKeyEqualTo(String value) {
            addCriterion("hash_key =", value, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyNotEqualTo(String value) {
            addCriterion("hash_key <>", value, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyGreaterThan(String value) {
            addCriterion("hash_key >", value, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyGreaterThanOrEqualTo(String value) {
            addCriterion("hash_key >=", value, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyLessThan(String value) {
            addCriterion("hash_key <", value, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyLessThanOrEqualTo(String value) {
            addCriterion("hash_key <=", value, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyLike(String value) {
            addCriterion("hash_key like", value, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyNotLike(String value) {
            addCriterion("hash_key not like", value, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyIn(List<String> values) {
            addCriterion("hash_key in", values, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyNotIn(List<String> values) {
            addCriterion("hash_key not in", values, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyBetween(String value1, String value2) {
            addCriterion("hash_key between", value1, value2, "hashKey");
            return (Criteria) this;
        }

        public Criteria andHashKeyNotBetween(String value1, String value2) {
            addCriterion("hash_key not between", value1, value2, "hashKey");
            return (Criteria) this;
        }

        public Criteria andTraceIdIsNull() {
            addCriterion("trace_id is null");
            return (Criteria) this;
        }

        public Criteria andTraceIdIsNotNull() {
            addCriterion("trace_id is not null");
            return (Criteria) this;
        }

        public Criteria andTraceIdEqualTo(String value) {
            addCriterion("trace_id =", value, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdNotEqualTo(String value) {
            addCriterion("trace_id <>", value, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdGreaterThan(String value) {
            addCriterion("trace_id >", value, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdGreaterThanOrEqualTo(String value) {
            addCriterion("trace_id >=", value, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdLessThan(String value) {
            addCriterion("trace_id <", value, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdLessThanOrEqualTo(String value) {
            addCriterion("trace_id <=", value, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdLike(String value) {
            addCriterion("trace_id like", value, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdNotLike(String value) {
            addCriterion("trace_id not like", value, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdIn(List<String> values) {
            addCriterion("trace_id in", values, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdNotIn(List<String> values) {
            addCriterion("trace_id not in", values, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdBetween(String value1, String value2) {
            addCriterion("trace_id between", value1, value2, "traceId");
            return (Criteria) this;
        }

        public Criteria andTraceIdNotBetween(String value1, String value2) {
            addCriterion("trace_id not between", value1, value2, "traceId");
            return (Criteria) this;
        }

        public Criteria andSendConfigIsNull() {
            addCriterion("send_config is null");
            return (Criteria) this;
        }

        public Criteria andSendConfigIsNotNull() {
            addCriterion("send_config is not null");
            return (Criteria) this;
        }

        public Criteria andSendConfigEqualTo(String value) {
            addCriterion("send_config =", value, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigNotEqualTo(String value) {
            addCriterion("send_config <>", value, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigGreaterThan(String value) {
            addCriterion("send_config >", value, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigGreaterThanOrEqualTo(String value) {
            addCriterion("send_config >=", value, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigLessThan(String value) {
            addCriterion("send_config <", value, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigLessThanOrEqualTo(String value) {
            addCriterion("send_config <=", value, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigLike(String value) {
            addCriterion("send_config like", value, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigNotLike(String value) {
            addCriterion("send_config not like", value, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigIn(List<String> values) {
            addCriterion("send_config in", values, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigNotIn(List<String> values) {
            addCriterion("send_config not in", values, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigBetween(String value1, String value2) {
            addCriterion("send_config between", value1, value2, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andSendConfigNotBetween(String value1, String value2) {
            addCriterion("send_config not between", value1, value2, "sendConfig");
            return (Criteria) this;
        }

        public Criteria andRetryNumIsNull() {
            addCriterion("retry_num is null");
            return (Criteria) this;
        }

        public Criteria andRetryNumIsNotNull() {
            addCriterion("retry_num is not null");
            return (Criteria) this;
        }

        public Criteria andRetryNumEqualTo(Integer value) {
            addCriterion("retry_num =", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumNotEqualTo(Integer value) {
            addCriterion("retry_num <>", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumGreaterThan(Integer value) {
            addCriterion("retry_num >", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_num >=", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumLessThan(Integer value) {
            addCriterion("retry_num <", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumLessThanOrEqualTo(Integer value) {
            addCriterion("retry_num <=", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumIn(List<Integer> values) {
            addCriterion("retry_num in", values, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumNotIn(List<Integer> values) {
            addCriterion("retry_num not in", values, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumBetween(Integer value1, Integer value2) {
            addCriterion("retry_num between", value1, value2, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_num not between", value1, value2, "retryNum");
            return (Criteria) this;
        }

        public Criteria andSendStatusIsNull() {
            addCriterion("send_status is null");
            return (Criteria) this;
        }

        public Criteria andSendStatusIsNotNull() {
            addCriterion("send_status is not null");
            return (Criteria) this;
        }

        public Criteria andSendStatusEqualTo(Integer value) {
            addCriterion("send_status =", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusNotEqualTo(Integer value) {
            addCriterion("send_status <>", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusGreaterThan(Integer value) {
            addCriterion("send_status >", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("send_status >=", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusLessThan(Integer value) {
            addCriterion("send_status <", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusLessThanOrEqualTo(Integer value) {
            addCriterion("send_status <=", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusIn(List<Integer> values) {
            addCriterion("send_status in", values, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusNotIn(List<Integer> values) {
            addCriterion("send_status not in", values, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusBetween(Integer value1, Integer value2) {
            addCriterion("send_status between", value1, value2, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("send_status not between", value1, value2, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}