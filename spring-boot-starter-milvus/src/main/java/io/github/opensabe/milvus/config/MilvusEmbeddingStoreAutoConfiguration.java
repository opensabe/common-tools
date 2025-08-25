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

import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.util.Optional;

import static io.github.opensabe.milvus.config.MilvusEmbeddingStoreProperties.DEFAULT_CONSISTENCY_LEVEL;
import static io.github.opensabe.milvus.config.MilvusEmbeddingStoreProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(MilvusEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class MilvusEmbeddingStoreAutoConfiguration {

    /**
     * 定义一个 embeddingStore，它是一个 ElasticsearchEmbeddingStore 实例
     *
     * @param properties     配置文件
     * @param embeddingModel 定义了一个 embeddingModel，它是一个 AllMiniLmL6V2EmbeddingModel 或者 AllMiniLmL6V2QuantizedEmbeddingModel 实例
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public MilvusEmbeddingStore milvusEmbeddingStore(MilvusEmbeddingStoreProperties properties,
                                                     @Nullable EmbeddingModel embeddingModel) {
        String collectionName = properties.getCollectionName();
        ConsistencyLevelEnum consistencyLevel = Optional.ofNullable(properties.getConsistencyLevel()).orElse(DEFAULT_CONSISTENCY_LEVEL);
        Integer dimension = Optional.ofNullable(properties.getDimension()).orElseGet(() -> embeddingModel == null ? null : embeddingModel.dimension());

        // 这里使用uri和token方式创建store实例，因为这是aws托管的zilliz
        MilvusEmbeddingStore embeddingStore = MilvusEmbeddingStore.builder()
                .collectionName(collectionName)
                // 这里的dimension表示向量维度，也就是数组的大小
                .dimension(dimension)
                .indexType(properties.getIndexType())
                .metricType(properties.getMetricType())
                .uri(properties.getUri())
                .token(properties.getToken())
                .consistencyLevel(consistencyLevel)
                .retrieveEmbeddingsOnSearch(properties.getRetrieveEmbeddingsOnSearch())
                .autoFlushOnInsert(properties.getAutoFlushOnInsert())
                .databaseName(properties.getDatabaseName())
                .build();
        return embeddingStore;
    }

    /**
     * 基本模型:基于 AllMiniLmL6V2 架构的语言模型
     * <p>
     * 1.没有经过量化,仍然使用浮点数表示参数。
     * 2.可能在某些情况下有更好的精度,因为没有量化引入的误差。
     * 3.可能更适合于需要更高精度的应用场景,如科学计算等。
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "langchain4j.milvus.quantized", havingValue = "false")
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * 量化嵌入模型:基于 AllMiniLmL6V2 架构的语言模型
     * <p>
     * 1.使用了量化技术,将模型参数压缩到更小的数据类型(如 int8),从而减小模型的大小和内存占用。
     * 2.在推理时通常更快,因为整数运算通常比浮点运算更高效。
     * 3.更适合于部署在资源受限的设备上,如移动设备或边缘设备。
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "langchain4j.milvus.quantized", havingValue = "true")
    public EmbeddingModel quantizedEmbeddingModel() {
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }

}
