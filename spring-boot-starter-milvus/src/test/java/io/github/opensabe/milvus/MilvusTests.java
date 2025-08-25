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
package io.github.opensabe.milvus;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.github.opensabe.milvus.config.MilvusEmbeddingStoreProperties;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 单测
 */
@Disabled
@SpringBootTest(classes = App.class)
@DisplayName("Milvus向量数据库测试")
class MilvusTests {

    @Autowired
    private MilvusEmbeddingStore embeddingStore;

    @Autowired
    private MilvusEmbeddingStoreProperties properties;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingModel quantizedEmbeddingModel;

    /**
     * 是一个占位方法，通过线程休眠来模拟等待数据持久化。
     */
    protected void awaitUntilPersisted() {

    }

    /**
     * 不启动量化测试。需要将配置中 langchain4j.milvus.quantized 设置为 false
     */
    @Test
    @DisplayName("测试非量化嵌入存储 - 验证向量存储和搜索功能")
    void should_provide_embedding_store_without_embedding_model() {

        // 每个TextSegment，就是一个句子，对应了一个向量，而向量就是一个数字数组
        TextSegment segment = TextSegment.from("hello");
        Embedding embedding = embeddingModel.embed(segment.text()).content();
        String id = embeddingStore.add(embedding, segment);
        Assertions.assertThat(id).isNotBlank();

        awaitUntilPersisted();

        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(EmbeddingSearchRequest.builder().queryEmbedding(embedding).maxResults(10).build()).matches();
        Assertions.assertThat(relevant).hasSize(1);
        EmbeddingMatch<TextSegment> match = (EmbeddingMatch)relevant.get(0);
        Assertions.assertThat(match.score()).isCloseTo(1.0, Percentage.withPercentage(1.0));
        Assertions.assertThat(match.embeddingId()).isEqualTo(id);
        Assertions.assertThat(match.embedding()).isEqualTo(embedding);
        Assertions.assertThat((TextSegment)match.embedded()).isEqualTo(segment);

        // 为了不影响下一个测试，这里删除本次embeddingStore创建的collection
        embeddingStore.dropCollection(properties.getCollectionName());
    }

    /**
     * 启动量化测试。需要将配置中 langchain4j.milvus.quantized 设置为 true
     */
    @Test
    @DisplayName("测试量化嵌入存储 - 验证量化向量存储和搜索功能")
    void should_provide_embedding_store_with_embedding_model() {

        TextSegment segment = TextSegment.from("hello");
        Embedding embedding = quantizedEmbeddingModel.embed(segment.text()).content();
        String id = embeddingStore.add(embedding, segment);
        Assertions.assertThat(id).isNotBlank();

        awaitUntilPersisted();

        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(embedding, 10);
        Assertions.assertThat(relevant).hasSize(1);
        EmbeddingMatch<TextSegment> match = relevant.get(0);
        Assertions.assertThat(match.score()).isCloseTo(1.0, Percentage.withPercentage(1.0));
        Assertions.assertThat(match.embeddingId()).isEqualTo(id);
        Assertions.assertThat(match.embedding()).isEqualTo(embedding);
        Assertions.assertThat(match.embedded()).isEqualTo(segment);

        // 为了不影响下一个测试，这里删除本次embeddingStore创建的collection
        embeddingStore.dropCollection(properties.getCollectionName());
    }

}
