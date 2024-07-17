package io.github.opensabe.mapstruct.core;

import io.github.opensabe.mapstruct.core.*;
import java.util.Map;
import java.util.HashMap;
<#list imports as i>
import ${i};
</#list>
public class MapperRepositoryImpl implements MapperRepository {

    private final Map<FromToKey, CommonCopyMapper> commonMapper = new HashMap<>();
    private final Map<Class, FromMapMapper> mapMapper = new HashMap<>();
    private final Map<Class, SelfCopyMapper> selfMapper = new HashMap<>();


    public MapperRepositoryImpl () {
        <#list common as c>
            commonMapper.put (new FromToKey(${c.from}.class, ${c.to}.class), new ${c.bean}Impl());
        </#list>

        <#list map as m>
            mapMapper.put (${m.from}.class, new ${m.bean}Impl());
        </#list>

        <#list self as s>
            selfMapper.put (${s.from}.class, new ${s.bean}Impl());
        </#list>
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> CommonCopyMapper<S, T> getMapper (Class<S> source, Class<T> target) {
        CommonCopyMapper mapper = null;
        if (source == target) {
            mapper = selfMapper.getOrDefault(source, selfMapper.get(target));
        }else {
            mapper = commonMapper.get(new FromToKey(source, target));
            if (mapper == null) {
                CommonCopyMapper<T, S> reverse = commonMapper.get(new FromToKey(target, source));
                if (reverse != null) {
                    mapper = new ReverseMapper<>(reverse);
                    commonMapper.put(new FromToKey(target, source), mapper);
                }
            }
        }
        if (mapper == null) {
            throw new MapperNotFoundException(source, target);
        }
        return (CommonCopyMapper<S, T>)mapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> FromMapMapper<T> getMapMapper (Class<T> target) {
        var mapper = mapMapper.get(target);
        if (mapper == null) {
            throw new MapperNotFoundException(target);
        }

        return (FromMapMapper<T>)mapper;
    }

    @Override
    public <S, T> void register(Class<S> source, Class<T> target, CommonCopyMapper<S, T> mapper) {
        commonMapper.put(new FromToKey(source, target), mapper);
    }

    @Override
    public <T> void register(Class<T> target, FromMapMapper<T> mapper) {
        mapMapper.put(target, mapper);
        }

    @Override
    public <T> void register(Class<T> target, SelfCopyMapper<T> mapper) {
        selfMapper.put(target, mapper);
    }
}