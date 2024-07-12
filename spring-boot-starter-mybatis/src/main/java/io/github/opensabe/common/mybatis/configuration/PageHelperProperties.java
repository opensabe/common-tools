package io.github.opensabe.common.mybatis.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

import static io.github.opensabe.common.mybatis.configuration.PageHelperProperties.PAGEHELPER_PREFIX;

@ConfigurationProperties(
        prefix = PAGEHELPER_PREFIX
)
public class PageHelperProperties {
    public static final String PAGEHELPER_PREFIX = "pagehelper";
    private Properties properties = new Properties();

    public PageHelperProperties() {
    }

    public Properties getProperties() {
        return this.properties;
    }

    public Boolean getOffsetAsPageNum() {
        return Boolean.valueOf(this.properties.getProperty("offsetAsPageNum"));
    }

    public void setOffsetAsPageNum(Boolean offsetAsPageNum) {
        this.properties.setProperty("offsetAsPageNum", offsetAsPageNum.toString());
    }

    public Boolean getRowBoundsWithCount() {
        return Boolean.valueOf(this.properties.getProperty("rowBoundsWithCount"));
    }

    public void setRowBoundsWithCount(Boolean rowBoundsWithCount) {
        this.properties.setProperty("rowBoundsWithCount", rowBoundsWithCount.toString());
    }

    public Boolean getPageSizeZero() {
        return Boolean.valueOf(this.properties.getProperty("pageSizeZero"));
    }

    public void setPageSizeZero(Boolean pageSizeZero) {
        this.properties.setProperty("pageSizeZero", pageSizeZero.toString());
    }

    public Boolean getReasonable() {
        return Boolean.valueOf(this.properties.getProperty("reasonable"));
    }

    public void setReasonable(Boolean reasonable) {
        this.properties.setProperty("reasonable", reasonable.toString());
    }

    public Boolean getSupportMethodsArguments() {
        return Boolean.valueOf(this.properties.getProperty("supportMethodsArguments"));
    }

    public void setSupportMethodsArguments(Boolean supportMethodsArguments) {
        this.properties.setProperty("supportMethodsArguments", supportMethodsArguments.toString());
    }

    public String getDialect() {
        return this.properties.getProperty("dialect");
    }

    public void setDialect(String dialect) {
        this.properties.setProperty("dialect", dialect);
    }

    public String getHelperDialect() {
        return this.properties.getProperty("helperDialect");
    }

    public void setHelperDialect(String helperDialect) {
        this.properties.setProperty("helperDialect", helperDialect);
    }

    public Boolean getAutoRuntimeDialect() {
        return Boolean.valueOf(this.properties.getProperty("autoRuntimeDialect"));
    }

    public void setAutoRuntimeDialect(Boolean autoRuntimeDialect) {
        this.properties.setProperty("autoRuntimeDialect", autoRuntimeDialect.toString());
    }

    public Boolean getAutoDialect() {
        return Boolean.valueOf(this.properties.getProperty("autoDialect"));
    }

    public void setAutoDialect(Boolean autoDialect) {
        this.properties.setProperty("autoDialect", autoDialect.toString());
    }

    public Boolean getCloseConn() {
        return Boolean.valueOf(this.properties.getProperty("closeConn"));
    }

    public void setCloseConn(Boolean closeConn) {
        this.properties.setProperty("closeConn", closeConn.toString());
    }

    public String getParams() {
        return this.properties.getProperty("params");
    }

    public void setParams(String params) {
        this.properties.setProperty("params", params);
    }

    public Boolean getDefaultCount() {
        return Boolean.valueOf(this.properties.getProperty("defaultCount"));
    }

    public void setDefaultCount(Boolean defaultCount) {
        this.properties.setProperty("defaultCount", defaultCount.toString());
    }

    public String getDialectAlias() {
        return this.properties.getProperty("dialectAlias");
    }

    public void setDialectAlias(String dialectAlias) {
        this.properties.setProperty("dialectAlias", dialectAlias);
    }
}

