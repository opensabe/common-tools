package io.github.opensabe.common.typehandler;

/**
 * 保存大json
 * @author hengma
 */
public interface OBSService {

    OBSTypeEnum type ();

    /**
     * 保存json字符串,返回的key需要区分type
     * 为了安群起见，子类也要加上关于事务的注解
     * @param json
     * @return 返回对应的key
     */
    void insert (String key, String json);


    /**
     * 通过key查询json字符串
     * @param key
     * @return
     */
    String select (String key);

}
