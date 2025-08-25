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
package io.github.opensabe.scheduler.conf;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "scheduler.job")
public class SchedulerProperties {
    /**
     * 是否启用 scheduler，默认启用，可以通过配置关闭
     */
    private boolean enable = true;
    private Long expiredTime;

    /**
     * 启动定时任务的业务线名称，这里会影响到多个task服务选择leader加锁的key值。
     * 这样多个不同的业务，可以在同一个redis集群中，根据各自所属业务线来选取任务执行的leader
     */
    private String businessLine = "";
}
