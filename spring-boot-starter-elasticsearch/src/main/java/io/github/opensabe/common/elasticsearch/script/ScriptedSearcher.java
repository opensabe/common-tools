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
package io.github.opensabe.common.elasticsearch.script;

import java.io.IOException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class ScriptedSearcher {
    private final ElasticsearchClient elasticsearchClient;

    public ScriptedSearcher(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public SearchResponse<JsonData> search(SearchRequest searchRequest, AbstractElasticSearchScript abstractElasticSearchScript, Object... objects) throws IOException {
        String script = abstractElasticSearchScript.getScript(objects);
        SearchRequest requestWithQuery = SearchRequest.of(s -> s
                .index(searchRequest.index())
                .preference(searchRequest.preference())
                .query(q -> q.wrapper(w -> w.query(script)))
        );
        SearchResponse<JsonData> search = elasticsearchClient.search(requestWithQuery, JsonData.class);
        if (search.took() > 3000L) {
            log.error("ScriptedSearcher-search took more than 3s");
        }
        return search;
    }
}
