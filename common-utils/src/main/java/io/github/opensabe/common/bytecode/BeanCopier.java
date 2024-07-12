package io.github.opensabe.common.bytecode;

/**
 * 属性复制service
 * @param <S>   源对象类型
 * @param <T>   目标对象类型
 * @author maheng
 */
public interface BeanCopier<S, T> {

    /**
     * 复制属性，将source中的属性复制到target对象中
     * <table border="1">
     * <tr><th>source property type</th><th>target property type</th><th>copy supported</th></tr>
     * <tr><td>{@code Integer}</td><td>{@code Integer}</td><td>yes</td></tr>
     * <tr><td>{@code Integer}</td><td>{@code Number}</td><td>yes</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<Integer>}</td><td>yes</td></tr>
     * <tr><td>{@code List<?>}</td><td>{@code List<?>}</td><td>yes</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<?>}</td><td>yes</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<? extends Number>}</td><td>yes</td></tr>
     * <tr><td>{@code String}</td><td>{@code Integer}</td><td>no</td></tr>
     * <tr><td>{@code Number}</td><td>{@code Integer}</td><td>no</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<Long>}</td><td>no</td></tr>
     * <tr><td>{@code List<Integer>}</td><td>{@code List<Number>}</td><td>no</td></tr>
     * </table>
     * @param source    源对象
     * @param target    目标对象
     */
    void copy (S source, T target);
}
