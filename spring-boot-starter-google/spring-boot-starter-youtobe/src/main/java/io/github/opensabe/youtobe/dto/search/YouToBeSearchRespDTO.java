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
package io.github.opensabe.youtobe.dto.search;

import java.util.List;
import io.github.opensabe.youtobe.dto.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * google data api 请求youtobe查询返回结果DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeSearchRespDTO {

    /**
     * 标识 API 资源类型。值为 youtube#searchListResponse。
     */
    private String kind;

    /**
     * 此资源的 Etag。
     */
    private String etag;

    /**
     * 可用作 pageToken 参数的值来检索结果集中的下一页的令牌。
     */
    private String nextPageToken;

    /**
     * 可用作 pageToken 参数的值来检索结果集中的上一页的令牌。
     */
    private String prevPageToken;

    /**
     * 搜索查询所用的地区代码。属性值是一个由两个字母构成的 ISO 国家/地区代码，用于标识区域。
     */
    private String regionCode;

    /**
     * 分页相关数据
     */
    private PageInfo pageInfo;

    /**
     * 与搜索条件匹配的结果列表。
     */
    private List<YouToBeSearchItemDTO> items;
}
