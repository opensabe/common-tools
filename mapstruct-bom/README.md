# 作用
- 在开发过程中，想把一个对象的属性值复制到另一个对象的相同字段，传统的方法 是通过get set方法，但是如果这两个对象字段多了，或者是
属性比较复杂，对象嵌套对象，对象属性里又有相同字段需要复制，这时候手写set就会变得很痛苦，这个框架是可以优雅得解决这个问题的。
- 对比Spring BeanUtils或者其他技术实现的变种，该框架的优势是我们可以知道，哪里用过对象里的字段

# 原理
- 在编译期间，自动生成代码，用get set方法来实现对象复制，因此，性能比其他通过反射来实现的框架要高很多，提前生成的代码可以享受到JIT优化加成
- 在开源项目 [mapstruct](https://github.com/mapstruct/mapstruct) 的基础上做了一些封装，上手更容易，同时也可以支持原生的mapstruct写法来实现更为复杂的功能

# 功能介绍

1. source object 转换成 target object
2. source object 转换成一个新的对象
3. map 转换成 对象

# 组件

对应的接口为:

```java
public interface CommonCopyMapper<S, T> {

    /**
     * source对象转换成target对象
     * @param source    instance of source
     * @return  instance of target
     */
    T map (S source);

    /**
     * target 对象转 source 对象
     * @param target    instance of target
     * @return  instance of source
     */
    S from (T target);

}

public interface FromMapMapper <T> {

    /**
     * 通过Map转成对象
     * <p>
     *     该转换不会deepClone, 只把Map中的key跟target中的field最对应，并把value强转成field类型
     * </p>
     * @param map map实例
     * @return  instance of target
     * @throws ClassCastException 如果key跟filed name相同，但是value跟filed类型不同
     */
    T fromMap (Map<String, Object> map);

}

public interface SelfCopyMapper<S> extends CommonCopyMapper<S, S> {

    @Override
    default S from(S target) {
        return map(target);
    }
}
```

# 快速上手

- 我们需要在相应转换的对象上添加 @Binding 注解即可

```java
@Getter
@Setter
@Binding(value = Student.class)
public class User {
    private String name;

    private Integer age;

    private Job job;
}

```
value可以为多个，意思是User对象可以复制成多种类型的对象

```
// 对象A转对象B
CommonCopyMapper<User, Student> mapper = MapperRepository.getInstance().getMapper(User.class, Student.class);
Student student = mapper.map(user);

// Map转对象
FromMapMapper<User, Student> mapper = MapperRepository.getInstance().getMapMapper(User.class);
User user = mapper.fromMap(Map.of("name","n1"));

//同类型对象复制
SelfCopyMapper<User> mapper = MapperRepository.getInstance().getMapper(User.class);
User user1 = mapper.map(user);
```

- @Binding 可以为我们生成基本的转换功能，如果我们需要复杂的功能，需要手写Mapper,又想添加到 MapperRepository

```java
@Mapper(uses= ObjectConverter.class )
@RegisterRepository
public interface JobMapper extends FromMapMapper<Job> {

}
```
如果在自己的Mapper上添加了 @RegisterRepository 注解，同时又实现了上面三个接口中的一个，那么，自定义的Mapper会替换掉自动生成的Mapper

# 使用

```java
    /**
     * transform source to target and return a new Object of target
     * @param source    source object
     * @param target    Type of target
     * @return          instance of target
     * @param <S>   Type of source
     * @param <T>   Type of target
     * @throws io.github.opensabe.mapstruct.core.MapperNotFoundException if source or target class not contains annotation of {@link io.github.opensabe.mapstruct.core.Binding}
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T transform (S source, Class<T> target) {
        return mapperRepository.getMapper((Class<S>) source.getClass(), target).map(source);
    }

    /**
     * create object by map
     * <p>
     *     key must marches of the target fields and the type of map value' type should same as field's type.
     * </p>
     * <b>no deep copy, so the map value(Map < String, Map < String, ?>) is not resolved.</b>
     * @param map       the map contains the field of target
     * @param target    target type
     * @return          instance of target
     * @param <T>       Type of target
     * @throws io.github.opensabe.mapstruct.core.MapperNotFoundException if target class not contains annotation of {@link io.github.opensabe.mapstruct.core.Binding}
     */
    public static <T> T fromMap (Map<String, Object> map, Class<T> target) {
        return mapperRepository.getMapMapper(target).fromMap(map);
    }
```
我们在使用的时候直接调用 BeanUtils.transform 以及 fromMap方法即可，如果遇到 MapperNotFoundException 异常，按照提示
查库source类上是否有@Binding注解，如果有，执行 mvn clean compile 即可解决
