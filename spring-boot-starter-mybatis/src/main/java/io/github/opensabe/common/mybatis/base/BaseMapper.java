package io.github.opensabe.common.mybatis.base;

import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * 通用mapper实现基本的方法
 * @author maheng
 */
public interface BaseMapper<T> extends Mapper<T>, InsertListMapper<T>,UpdateLimitMapper<T> {
}
