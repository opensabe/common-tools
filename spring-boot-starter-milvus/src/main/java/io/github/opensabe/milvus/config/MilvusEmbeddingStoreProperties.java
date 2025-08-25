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
package io.github.opensabe.milvus.config;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.milvus.common.clientenum.ConsistencyLevelEnum.STRONG;

@Getter
@Setter
@ConfigurationProperties(prefix = MilvusEmbeddingStoreProperties.PREFIX)
public class MilvusEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.milvus";
    static final ConsistencyLevelEnum DEFAULT_CONSISTENCY_LEVEL = STRONG;

//    private String host;
//    private Integer port;
    private String collectionName;
    private Integer dimension;
    private IndexType indexType;
    private MetricType metricType;

    /**
     * aws中zilliz的Public Endpoint值
     */
    private String uri;

    /**
     * aws中zilliz的Token值
     */
    private String token;

//    private String username;
//    private String password;
    private ConsistencyLevelEnum consistencyLevel;
    private Boolean retrieveEmbeddingsOnSearch;
    private Boolean autoFlushOnInsert;
    private String databaseName;

    /**
     * 增加配置，用来控制是否启用量化的EmbeddingModel，默认不起用量化
     */
    private Boolean quantized = false;
}
