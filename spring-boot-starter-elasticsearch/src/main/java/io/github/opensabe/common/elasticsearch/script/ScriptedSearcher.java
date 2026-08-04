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

/**
 * 基于 wrapper query 的脚本化 ES 搜索门面。
 * <p>
 * 仅转发原 {@link SearchRequest} 的 {@code index} 与 {@code preference}；
 * {@code size}、{@code sort}、{@code aggs} 等其余字段不会透传，调用方勿依赖。
 * </p>
 */
@Log4j2
public class ScriptedSearcher {

    /**
     * 底层 Elasticsearch 高级客户端。
     */
    private final ElasticsearchClient elasticsearchClient;

    /**
     * @param elasticsearchClient ES 客户端
     */
    public ScriptedSearcher(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * 用脚本生成 wrapper query 并执行搜索；耗时超过 3s 时记录 error 日志。
     *
     * @param searchRequest              原始搜索请求（仅 index/preference 被复用）
     * @param abstractElasticSearchScript 脚本提供者
     * @param objects                    脚本参数
     * @return ES 搜索响应
     * @throws IOException 客户端 IO 失败时
     */
    public SearchResponse<JsonData> search(SearchRequest searchRequest, AbstractElasticSearchScript abstractElasticSearchScript, Object... objects) throws IOException {
        String script = abstractElasticSearchScript.getScript(objects);
        SearchRequest requestWithQuery = SearchRequest.of(s -> s
                .index(searchRequest.index())
                .preference(searchRequest.preference())
                .query(q -> q.wrapper(w -> w.query(script)))
        );
        SearchResponse<JsonData> search = elasticsearchClient.search(requestWithQuery, JsonData.class);
        if (search.took() > 3000L) {
            log.error("ScriptedSearcher search took more than 3s");
        }
        return search;
    }
}
