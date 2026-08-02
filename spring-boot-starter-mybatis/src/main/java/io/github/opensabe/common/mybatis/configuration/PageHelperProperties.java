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
package io.github.opensabe.common.mybatis.configuration;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.github.opensabe.common.mybatis.configuration.PageHelperProperties.PAGEHELPER_PREFIX;

/**
 * PageHelper 配置属性。
 */
@ConfigurationProperties(
        prefix = PAGEHELPER_PREFIX
)
public class PageHelperProperties {
    public static final String PAGEHELPER_PREFIX = "pagehelper";
    private Properties properties = new Properties();

    public PageHelperProperties() {
    }

    /**
     * @return properties
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * @return offsetAsPageNum
     */
    public Boolean getOffsetAsPageNum() {
        return Boolean.valueOf(this.properties.getProperty("offsetAsPageNum"));
    }

    /**
     * @param offsetAsPageNum 待设置值
     */
    public void setOffsetAsPageNum(Boolean offsetAsPageNum) {
        this.properties.setProperty("offsetAsPageNum", offsetAsPageNum.toString());
    }

    /**
     * @return rowBoundsWithCount
     */
    public Boolean getRowBoundsWithCount() {
        return Boolean.valueOf(this.properties.getProperty("rowBoundsWithCount"));
    }

    /**
     * @param rowBoundsWithCount 待设置值
     */
    public void setRowBoundsWithCount(Boolean rowBoundsWithCount) {
        this.properties.setProperty("rowBoundsWithCount", rowBoundsWithCount.toString());
    }

    /**
     * @return pageSizeZero
     */
    public Boolean getPageSizeZero() {
        return Boolean.valueOf(this.properties.getProperty("pageSizeZero"));
    }

    /**
     * @param pageSizeZero 待设置值
     */
    public void setPageSizeZero(Boolean pageSizeZero) {
        this.properties.setProperty("pageSizeZero", pageSizeZero.toString());
    }

    /**
     * @return reasonable
     */
    public Boolean getReasonable() {
        return Boolean.valueOf(this.properties.getProperty("reasonable"));
    }

    /**
     * @param reasonable 待设置值
     */
    public void setReasonable(Boolean reasonable) {
        this.properties.setProperty("reasonable", reasonable.toString());
    }

    /**
     * @return supportMethodsArguments
     */
    public Boolean getSupportMethodsArguments() {
        return Boolean.valueOf(this.properties.getProperty("supportMethodsArguments"));
    }

    /**
     * @param supportMethodsArguments 待设置值
     */
    public void setSupportMethodsArguments(Boolean supportMethodsArguments) {
        this.properties.setProperty("supportMethodsArguments", supportMethodsArguments.toString());
    }

    /**
     * @return dialect
     */
    public String getDialect() {
        return this.properties.getProperty("dialect");
    }

    /**
     * @param dialect 待设置值
     */
    public void setDialect(String dialect) {
        this.properties.setProperty("dialect", dialect);
    }

    /**
     * @return helperDialect
     */
    public String getHelperDialect() {
        return this.properties.getProperty("helperDialect");
    }

    /**
     * @param helperDialect 待设置值
     */
    public void setHelperDialect(String helperDialect) {
        this.properties.setProperty("helperDialect", helperDialect);
    }

    /**
     * @return autoRuntimeDialect
     */
    public Boolean getAutoRuntimeDialect() {
        return Boolean.valueOf(this.properties.getProperty("autoRuntimeDialect"));
    }

    /**
     * @param autoRuntimeDialect 待设置值
     */
    public void setAutoRuntimeDialect(Boolean autoRuntimeDialect) {
        this.properties.setProperty("autoRuntimeDialect", autoRuntimeDialect.toString());
    }

    /**
     * @return autoDialect
     */
    public Boolean getAutoDialect() {
        return Boolean.valueOf(this.properties.getProperty("autoDialect"));
    }

    /**
     * @param autoDialect 待设置值
     */
    public void setAutoDialect(Boolean autoDialect) {
        this.properties.setProperty("autoDialect", autoDialect.toString());
    }

    /**
     * @return closeConn
     */
    public Boolean getCloseConn() {
        return Boolean.valueOf(this.properties.getProperty("closeConn"));
    }

    /**
     * @param closeConn 待设置值
     */
    public void setCloseConn(Boolean closeConn) {
        this.properties.setProperty("closeConn", closeConn.toString());
    }

    /**
     * @return params
     */
    public String getParams() {
        return this.properties.getProperty("params");
    }

    /**
     * @param params 待设置值
     */
    public void setParams(String params) {
        this.properties.setProperty("params", params);
    }

    /**
     * @return defaultCount
     */
    public Boolean getDefaultCount() {
        return Boolean.valueOf(this.properties.getProperty("defaultCount"));
    }

    /**
     * @param defaultCount 待设置值
     */
    public void setDefaultCount(Boolean defaultCount) {
        this.properties.setProperty("defaultCount", defaultCount.toString());
    }

    /**
     * @return dialectAlias
     */
    public String getDialectAlias() {
        return this.properties.getProperty("dialectAlias");
    }

    /**
     * @param dialectAlias 待设置值
     */
    public void setDialectAlias(String dialectAlias) {
        this.properties.setProperty("dialectAlias", dialectAlias);
    }
}

