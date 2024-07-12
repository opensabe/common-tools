package io.github.opensabe.common.mybatis.configuration;

import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.typehandler.OBSService;
import io.github.opensabe.common.typehandler.OBSTypeEnum;
import io.github.opensabe.common.utils.SpringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 因为OBSTypeHandler每次都需要创建新实例，将需要用的的bean保存到静态变量中提高效率
 * @author hengma
 */
public class TypeHandlerSpringHolderConfiguration  {

    private final static Map<OBSTypeEnum, OBSService> obsServices = new HashMap<>();
    private static UniqueID uniqueID;
    public static OBSService getService (OBSTypeEnum type) {
        if (obsServices.containsKey(type)) {
            return obsServices.get(type);
        }
        var service = SpringUtil.getApplicationContext().getBeanProvider(OBSService.class)
                .stream().filter(s -> Objects.equals(type, s.type()))
                .findFirst()
                .orElse(null);
        obsServices.put(type, service);
        return service;
    }

    public static UniqueID getUniqueID () {
        if (Objects.isNull(uniqueID)) {
            uniqueID = SpringUtil.getBean(UniqueID.class);
        }
        return uniqueID;
    }
}
