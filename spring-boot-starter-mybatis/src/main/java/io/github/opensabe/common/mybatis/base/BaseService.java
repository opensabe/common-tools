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
package io.github.opensabe.common.mybatis.base;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.ibatis.binding.MapperProxy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Table;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.weekend.Fn;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.reflection.Reflections;

import static java.lang.String.format;

/**
 * baseService,实现大部分的单表操作
 *
 * @author maheng
 */
@SuppressWarnings("resource")
public abstract class BaseService<T> {
    private final Class<T> entityClass;
    protected org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName());
    private BaseMapper<T> mapper;
    @Autowired
    private ObjectProvider<BaseMapper<T>> provider;

    @SuppressWarnings("unchecked")
    public BaseService() {
        log.debug("init service {}", this.getClass());
        //考虑多重继承的情况
        Class<?> aClass = this.getClass();
        while (aClass.getSuperclass() != null && !aClass.getSuperclass().equals(Object.class) && !aClass.getSuperclass().equals(BaseService.class)
                && !Modifier.isAbstract(aClass.getSuperclass().getModifiers())) {
            aClass = aClass.getSuperclass();
        }
        Type type = aClass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            this.entityClass = (Class<T>) types[0];
        } else {
            throw new RuntimeException(format("service %s not assign parameter type", this.getClass()));
        }
    }


    @SuppressWarnings("all")
    public BaseService<T> readOnly(boolean isReadOnly) {
        if (isReadOnly) {
            DynamicRoutingDataSource.setDataSourceRW("read");
        } else {
            DynamicRoutingDataSource.setDataSourceRW("write");
        }
        return this;
    }

    @PostConstruct
    public void initMapper() {
        var mapper = getMapper();
        if (Objects.isNull(mapper)) {
            mapper = provider.getIfUnique();
            if (Objects.isNull(mapper))
                mapper = provider.orderedStream()
                        .filter(mm -> {
                            var hf = ReflectionUtils.findField(mm.getClass(), "h");
                            ReflectionUtils.makeAccessible(Objects.requireNonNull(hf));
                            var m = ReflectionUtils.getField(hf, mm);

                            if (m instanceof @SuppressWarnings("rawtypes")MapperProxy proxy) {
                                var filed = ReflectionUtils.findField(proxy.getClass(), "mapperInterface", Class.class);
                                ReflectionUtils.makeAccessible(Objects.requireNonNull(filed));
                                var mapperClass = (Class<?>) ReflectionUtils.getField(filed, m);
                                if (mapperClass == null) return false;
                                var mapperName = mapperClass.getSimpleName().replace("Mapper", "");
                                var serviceName = this.getClass().getSimpleName().replace("Service", "");
                                return mapperName.equals(serviceName);
                            }
                            return false;
                        })
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("no mapper find for " + entityClass.getName()));
        }
        this.mapper = mapper;
    }

    protected BaseMapper<T> getMapper() {
        return null;
    }

    /**
     * 获取表名称
     *
     * @return
     */
    public String getTableName() {
        return entityClass.getAnnotation(Table.class).name();
    }

    /**
     * 获取表字段
     *
     * @param fn 对应的field的getter
     * @return 表字段名称
     */
    public String getField(Fn<T, Object> fn) {
        return CommonProvider.camelToUnderScore(Reflections.fnToFieldName(fn));
    }

    /**
     * 获取Weekend需要的字段名
     *
     * @param fn 对应的field的getter
     * @return
     */
    public String getFieldForWeekend(Fn<T, Object> fn) {
        return Reflections.fnToFieldName(fn);
    }

    /**
     * 获取表名称.字段，用于join
     *
     * @param fn 对应的field的getter
     * @return 表名称.字段
     */
    public String getTableField(Fn<T, Object> fn) {
        return getTableName() + "." + getField(fn);
    }

    /**
     * 通过主键查询
     *
     * @param id 主键
     * @return entity对象
     */
    public T selectById(Object id) {
        return mapper.selectByPrimaryKey(id);
    }

    public T selectByIdReadOnly(Object id) {
        readOnly(true);
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * @param record 查询条件
     * @return 数量
     */
    public long selectCount(T record) {
        return mapper.selectCount(record);
    }

    public long selectCountReadOnly(T record) {
        readOnly(true);
        return mapper.selectCount(record);
    }

    /**
     * 根据条件简单查询
     *
     * @param record 查询条件，按照=查询
     * @return entity集合
     */
    public List<T> select(T record) {
        return mapper.select(record);
    }

    public List<T> selectReadOnly(T record) {
        readOnly(true);
        return mapper.select(record);
    }

    /**
     * 分页查询，并排序
     *
     * @param record   查询条件
     * @param pageNum  当前页数
     * @param pageSize 每页大小
     * @param order    排序，key为entity属性名，value为desc或者空
     * @return 封装好的pageInfo
     */
    public Page<T> select(T record, int pageNum, int pageSize, Map<String, String> order) {
        String orderBy = orderBy(order);
        return PageHelper.startPage(pageNum, pageSize, orderBy)
                .doSelectPage(() -> mapper.select(record));
    }

    public Page<T> selectReadOnly(T record, int pageNum, int pageSize, Map<String, String> order) {
        String orderBy = orderBy(order);
        return PageHelper.startPage(pageNum, pageSize, orderBy)
                .doSelectPage(() -> {
                    readOnly(true);
                    mapper.select(record);
                });
    }

    /**
     * 分页查询，不排序
     *
     * @param record   查询条件
     * @param pageNum  页数
     * @param pageSize 每页大小
     * @return
     */
    public Page<T> select(T record, int pageNum, int pageSize) {
        return select(record, pageNum, pageSize, null);
    }

    public Page<T> selectReadOnly(T record, int pageNum, int pageSize) {
        readOnly(true);
        return select(record, pageNum, pageSize, null);
    }

    /**
     * 根据顺序查询第一条记录
     *
     * @param record 查询条件
     * @param order  顺序
     * @return
     */
    public T selectOne(T record, Map<String, String> order) {
        var list = selectLimit(record, order, 1);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public T selectOneReadOnly(T record, Map<String, String> order) {
        readOnly(true);
        return selectOne(record, order);
    }

    /**
     * 限制返回的数量
     *
     * @param record 查询条件
     * @param limit  最大返回多少条记录
     * @param order  排序
     * @return
     */
    public List<T> selectLimit(T record, Map<String, String> order, int limit) {
        String orderBy = orderBy(order);
        return PageHelper.offsetPage(0, limit, false)
                .setOrderBy(orderBy)
                .doSelectPage(() -> mapper.select(record));
    }

    public List<T> selectLimitReadOnly(T record, Map<String, String> order, int limit) {
        readOnly(true);
        return selectLimit(record, order, limit);
    }

    public List<T> selectLimit(T record, int limit) {
        return selectLimit(record, null, limit);
    }

    public List<T> selectLimitReadOnly(T record, int limit) {
        readOnly(true);
        return selectLimit(record, null, limit);
    }

    /**
     * 通过weekend查询第一条
     *
     * @param weekend 查询条件
     * @param order   顺序
     * @return
     */
    public T selectOneByExample(Weekend<T> weekend, Map<String, String> order) {
        var list = selectLimitByExample(weekend, order, 1);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public T selectOneByExampleReadOnly(Weekend<T> weekend, Map<String, String> order) {
        readOnly(true);
        return selectOneByExample(weekend, order);
    }

    /**
     * 通过weekend查询指定数量的结果
     *
     * @param weekend 查询条件
     * @param order   顺序
     * @param limit   最多返回多少数据
     * @return
     */
    public List<T> selectLimitByExample(Weekend<T> weekend, Map<String, String> order, int limit) {
        String orderBy = orderBy(order);
        return PageHelper.offsetPage(0, limit, false)
                .setOrderBy(orderBy)
                .doSelectPage(() -> mapper.selectByExample(weekend));
    }

    public List<T> selectLimitByExampleReadOnly(Weekend<T> weekend, Map<String, String> order, int limit) {
        readOnly(true);
        return selectLimitByExample(weekend, order, limit);
    }

    public List<T> selectLimitByExample(Weekend<T> weekend, int limit) {
        return selectLimitByExample(weekend, null, limit);
    }

    public List<T> selectLimitByExampleReadOnly(Weekend<T> weekend, int limit) {
        readOnly(true);
        return selectLimitByExample(weekend, null, limit);
    }

    /**
     *
     * @param weekend
     * @return
     */
    public T selectOneByExample(Weekend<T> weekend) {
        return selectOneByExample(weekend, null);
    }

    public T selectOneByExampleReadOnly(Weekend<T> weekend) {
        readOnly(true);
        return selectOneByExample(weekend, null);
    }

    /**
     * 查询第一条数据
     *
     * @param record
     * @return
     */
    public T selectOne(T record) {
        return selectOne(record, null);
    }

    public T selectOneReadOnly(T record) {
        readOnly(true);
        return selectOne(record, null);
    }

    /**
     * 通过SQL查询
     * <pre>
     *  Weekend<Student> weekend = Weekend.of(Student.class);
     *  weekend.weekendCriteria().andIn(Student::getId,Arrays.asList(1,2,3,4))
     *  selectByExample(weekend)
     * </pre>
     *
     * @param weekend SQL
     * @return
     */
    public List<T> selectByExample(Weekend<T> weekend) {
        return mapper.selectByExample(weekend);
    }

    public List<T> selectByExampleReadOnly(Weekend<T> weekend) {
        readOnly(true);
        return mapper.selectByExample(weekend);
    }

    public Page<T> selectByExample(Weekend<T> weekend, int pageNum, int pageSize, Map<String, String> order) {
        String orderQuery = orderBy(order);
        return PageHelper.startPage(pageNum, pageSize, orderQuery).
                doSelectPage(() -> mapper.selectByExample(weekend));
    }

    public Page<T> selectByExampleReadOnly(Weekend<T> weekend, int pageNum, int pageSize, Map<String, String> order) {
        readOnly(true);
        return selectByExample(weekend, pageNum, pageSize, order);
    }

    public long selectCountByExample(Weekend<T> weekend) {
        return mapper.selectCountByExample(weekend);
    }

    public long selectCountByExampleReadOnly(Weekend<T> weekend) {
        readOnly(true);
        return mapper.selectCountByExample(weekend);
    }

    /**
     * 通过主键修改
     *
     * @param record 主键必须有值
     * @return 影响的结果数
     */
    public int updateByIdSelective(T record) {
        return mapper.updateByPrimaryKeySelective(record);
    }

    public int updateByExample(T record, Weekend<T> weekend) {
        return mapper.updateByExampleSelective(record, weekend);
    }

    public int updateByExampleLimit(T record, Weekend<T> weekend, int limit) {
        return mapper.updateByExampleSelectiveLimit(record, weekend, limit);
    }

    @SuppressWarnings("all")
    public int insertSelective(T record) {
        return mapper.insertSelective(record);
    }

    /**
     * 批量插入
     *
     * @param list 要插入的集合
     * @return 影响的记录数
     */
    @SuppressWarnings("all")
    public int insertList(List<T> list) {
        return mapper.insertList(list);
    }

    private String orderBy(Map<String, String> order) {
        if (order != null) {
            Map<String, EntityColumn> propertyMap = EntityHelper.getEntityTable(entityClass).getPropertyMap();
            StringBuilder orderQuery = new StringBuilder();
            order.forEach((k, v) -> {
                EntityColumn entityColumn = propertyMap.get(k);
                if (entityColumn != null) {
                    orderQuery.append(entityColumn.getColumn()).append(" ").append(v).append(",");
                }
            });
            return orderQuery.substring(0, orderQuery.length() - 1);
        }
        return null;
    }
}
