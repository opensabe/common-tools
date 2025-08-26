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
package io.github.opensabe.spring.cloud.parent.common.eureka;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;

import io.github.opensabe.common.utils.AlarmUtil;
import lombok.extern.log4j.Log4j2;

/**
 * 将环境变量中 NODE_NAME 加入 eureka 注册实例信息中的 metadata
 * NODE_NAME 是运维团队在 k8s pod 中加入的环境变脸
 */
@Log4j2
public class EurekaInstanceConfigBeanAddNodeInfoCustomizer implements EurekaInstanceConfigBeanCustomizer {
    public static final String K8S_NODE_INFO = "k8s-node-info";
    private static final String SYSTEM_VARIABLE = "NODE_NAME";

    @Override
    public void customize(EurekaInstanceConfigBean eurekaInstanceConfigBean) {
        String nodeName = System.getenv(SYSTEM_VARIABLE);
        if (StringUtils.isNotBlank(nodeName)) {
            log.info("EurekaInstanceConfigBeanAddNodeInfoCustomizer-customize: found k8s-node-info: {}", nodeName);
            eurekaInstanceConfigBean.getMetadataMap().put(K8S_NODE_INFO, nodeName);
        } else {
            AlarmUtil.fatal("EurekaInstanceConfigBeanAddNodeInfoCustomizer-customize: not found k8s-node-info from system variable: {}", SYSTEM_VARIABLE);
        }
    }
}
