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
package io.github.opensabe.common.elasticsearch.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import io.github.opensabe.common.testcontainers.integration.SingleElasticSearchIntegrationTest;
import lombok.extern.log4j.Log4j2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = ElasticClientTest.Main.class,
        properties = "eureka.client.enabled=false"
)
@ExtendWith({
        SingleElasticSearchIntegrationTest.class,
        SpringExtension.class
})
@Log4j2
@DisplayName("Elasticsearch客户端测试")
public class ElasticClientTest {
    private static final String INDEX = "test_index";
    private static final String SECRET = "secretString";
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        SingleElasticSearchIntegrationTest.setProperties(registry);
    }

    @Test
    @DisplayName("测试Elasticsearch基本操作 - 索引创建、文档更新、搜索和敏感信息过滤")
    public void test() throws IOException, InterruptedException {
        boolean exists = elasticsearchClient.indices().exists(e -> e.index(INDEX)).value();
        assertFalse(exists);

        var createIndexResponse = elasticsearchClient.indices().create(c -> c
                .index(INDEX)
                .withJson(new StringReader(
                        "{\n" +
                                "  \"settings\": {\n" +
                                "    \"index\": {\n" +
                                "      \"refresh_interval\": \"1s\",\n" +
                                "      \"number_of_shards\": \"1\",\n" +
                                "      \"number_of_replicas\": \"1\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"mappings\": {\n" +
                                "    \"properties\": {\n" +
                                "      \"name\": {\n" +
                                "        \"type\": \"text\"\n" +
                                "      },\n" +
                                "      \"id\": {\n" +
                                "        \"type\": \"keyword\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"aliases\": {}\n" +
                                "}"
                ))
        );
        assertTrue(createIndexResponse.acknowledged());

        Map<String, String> obj = Map.of("name", "test name", "id", "id1");
        Map<String, String> upsertDoc = obj;
        var update = elasticsearchClient.update(u -> u
                        .index(INDEX)
                        .id("id1")
                        .doc(upsertDoc)
                        .upsert(upsertDoc)
                        .docAsUpsert(true)
                        .retryOnConflict(3),
                Map.class
        );
        assertEquals("id1", update.id());

        Map<String, String> secretObj = Map.of("name", SECRET, "id", "id2");
        assertThrows(RuntimeException.class, () -> elasticsearchClient.update(u -> u
                        .index(INDEX)
                        .id("id2")
                        .doc(secretObj)
                        .upsert(secretObj)
                        .docAsUpsert(true)
                        .retryOnConflict(3),
                Map.class
        ));

        // Wait for the document to be indexed
        TimeUnit.SECONDS.sleep(3);

        SearchResponse<Map> search = elasticsearchClient.search(s -> s
                        .index(INDEX)
                        .preference("_local")
                        .query(q -> q.matchAll(m -> m)),
                Map.class
        );
        assertEquals(1, search.hits().hits().size());
        Hit<Map> hit = search.hits().hits().get(0);
        assertEquals("test name", hit.source().get("name"));
        assertEquals("id1", hit.source().get("id"));
    }

    @SpringBootApplication
    public static class Main {
        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }

    public static class TestSecretProvider extends SecretProvider {
        protected TestSecretProvider(GlobalSecretManager globalSecretManager) {
            super(globalSecretManager);
        }

        @Override
        protected String name() {
            return "testSecretProvider";
        }

        @Override
        protected long reloadTimeInterval() {
            return 1;
        }

        @Override
        protected TimeUnit reloadTimeIntervalUnit() {
            return TimeUnit.DAYS;
        }

        @Override
        protected Map<String, Set<String>> reload() {
            return Map.of(
                    "testSecretProviderKey", Set.of(SECRET)
            );
        }
    }
}
