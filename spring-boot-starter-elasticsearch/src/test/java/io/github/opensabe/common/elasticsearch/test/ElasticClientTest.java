package io.github.opensabe.common.elasticsearch.test;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import io.github.opensabe.common.testcontainers.integration.SingleElasticSearchIntegrationTest;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = ElasticClientTest.Main.class
)
@ExtendWith({
        SingleElasticSearchIntegrationTest.class,
        SpringExtension.class
})
@Log4j2
public class ElasticClientTest {
    @SpringBootApplication
    public static class Main {
        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        SingleElasticSearchIntegrationTest.setProperties(registry);
    }

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final String INDEX = "test_index";

    @Test
    public void test() throws IOException, InterruptedException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX);
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        assertFalse(exists);

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);
        createIndexRequest.source(
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
                , XContentType.JSON
        );
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices()
                .create(createIndexRequest, RequestOptions.DEFAULT);
        assertTrue(createIndexResponse.isAcknowledged());

        UpdateRequest updateRequest = new UpdateRequest(INDEX, "id1");
        Map<String, String> obj = Map.of("name", "test name", "id", "id1");
        String jsonString = JsonUtil.toJSONString(obj);
        updateRequest.doc(jsonString, XContentType.JSON);
        updateRequest.fetchSource(false);
        updateRequest.upsert(jsonString, XContentType.JSON);
        updateRequest.retryOnConflict(3);
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        assertEquals("id1", update.getId());

        obj = Map.of("name", SECRET, "id", "id2");
        jsonString = JsonUtil.toJSONString(obj);
        updateRequest = new UpdateRequest(INDEX, "id2");
        updateRequest.doc(jsonString, XContentType.JSON);
        updateRequest.fetchSource(false);
        updateRequest.upsert(jsonString, XContentType.JSON);
        updateRequest.retryOnConflict(3);
        UpdateRequest finalUpdateRequest = updateRequest;
        assertThrows(RuntimeException.class, () -> restHighLevelClient.update(finalUpdateRequest, RequestOptions.DEFAULT));

        // Wait for the document to be indexed
        TimeUnit.SECONDS.sleep(3);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().fetchSource(true);
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(matchAllQueryBuilder);
        SearchRequest searchRequest = new SearchRequest()
                .indices(INDEX)
                .source(searchSourceBuilder)
                .preference("_local");
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = search.getHits().getHits();
        assertEquals(1, hits.length);
        assertEquals("test name", hits[0].getSourceAsMap().get("name"));
        assertEquals("id1", hits[0].getSourceAsMap().get("id"));
    }

    private static final String SECRET = "secretString";

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
