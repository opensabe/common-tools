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
package io.github.opensabe.common.mybatis.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @see https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE%E5%B1%9E%E6%80%A7%E5%88%97%E8%A1%A8
 */
@Getter
@Setter
@ToString
public class DataSourceProperties {

    /**
     * jdbc
     */
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String name;
    private Boolean isWriteAllowed = true;
    private String clusterName = "";

    /**
     * StatFilter
     */
    private boolean logSlowSql = true;
    private long slowSqlMillis = 500L;
    private boolean mergeSql = true;
    /**
     * 修改 level 为 WARN
     */
    private String slowSqlLogLevel = "WARN";
    /**
     * 报警记录区间，默认 30s
     */
    private long alarmIntervalInSeconds = 30;
    /**
     * 某个慢 SQL 在 alarmIntervalInSeconds 内超过 alarmThreshold 次，就报警
     * 默认是 30s 超过 20 次才会报警
     */
    private int alarmThreshold = 20;

    /**
     * Log4j2Filter
     */


    private boolean dataSourceLogEnabled = true;
    private boolean connectionLogEnabled = true;
    private boolean connectionLogErrorEnabled = true;
    private boolean statementLogEnabled = true;
    private boolean statementLogErrorEnabled = false;
    private boolean resultSetLogEnabled = true;
    private boolean resultSetLogErrorEnabled = true;

    /**
     * WallFilter，如果遇到限制，暂时不抛异常，只打印日志：
     * sql injection violation, dbTyp  ..........
     */

    private boolean logViolation = true;
    private boolean throwException = false;

    private boolean truncateAllow = false;
    private boolean createTableAllow = false;
    private boolean dropTableAllow = false;
    private boolean alterTableAllow = false;
    private boolean renameTableAllow = false;
    private boolean lockTableAllow = false;
    private boolean startTransactionAllow = true;
    private boolean blockAllow = false;
    private boolean intersectAllow = true;

    /**
     * 是否允许非以上基本语句的其他语句，通过这个选项就能够屏蔽DDL。
     */
    private boolean noneBaseStatementAllow = true;


    private boolean selectWhereAlwayTrueCheck = true;
    private boolean selectHavingAlwayTrueCheck = true;

    private boolean conditionAndAlwayTrueAllow = true;
    private boolean conditionAndAlwayFalseAllow = true;
    private boolean conditionLikeTrueAllow = true;

    private boolean deleteWhereAlwayTrueCheck = false;
    private boolean deleteWhereNoneCheck = false;

    private boolean updateWhereAlayTrueCheck = false;
    private boolean updateWhereNoneCheck = false;


    /**
     * 是否允许一次执行多条语句
     */
    private boolean multiStatementAllow = true;
    /**
     * 是否允许语句中存在注释，Oracle的用户不用担心，Wall能够识别hints和注释的区别, mysql必须允许
     */
    private boolean commentAllow = true;

    /**
     * 是否必须参数化，如果为True，则不允许类似WHERE ID = 1这种不参数化的SQL
     */
    private boolean mustParameterized = false;

    /**
     * 是否进行严格的语法检测，Druid SQL Parser在某些场景不能覆盖所有的SQL语法，出现解析SQL出错，可以临时把这个选项设置为false，同时把SQL反馈给Druid的开发者。
     */
    private boolean strictSyntaxCheck = false;

    private int selectLimit = -1;


    /**
     * data source
     */
    private int initialSize = 20;
    private int minIdle = 20;
    private int maxActive = 500;
    private long maxWait = 3000;
    private boolean useUnfairLock = true;
    private String validationQuery = "select 1";
    /**
     * 2023年06月27日15:38:33
     */
    private int validationQueryTimeout = -1;

    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;
    private boolean testWhileIdle = true;
    private long timeBetweenEvictionRunsMillis = 60000;
    private long minEvictableIdleTimeMillis = 1000 * 60 * 30;

    /**
     * 在mysql下建议关闭。是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。
     */
    private boolean poolPreparedStatements = false;
    /**
     * 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
     */
    private int maxPoolPreparedStatementPerConnectionSize = -1;

    private int maxOpenPreparedStatements = 50;
    private long timeBetweenLogStatsMillis = 60000L;

    /**
     * @since druid 1.2.12
     * 2023年06月27日15:38:58
     */
    private int connectTimeout = 10_000; // milliSeconds
    private int socketTimeout = 24 * 3600 * 1000; // milliSeconds

    private boolean usePingMethod = false;

}
