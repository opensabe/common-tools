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
package io.github.opensabe.youtobe.dto.list;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * google data api 请求youtobe查询入参DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeListReqDTO {

    /**
     * 核心：视频id
     * id 参数指定要检索的资源的YouTube视频ID。逗号分隔列表。在video资源中，id属性用于指定视频的ID。
     * 如：Ks-_Mh1QhMc,c0KYU2j0TM4,eIho2S0ZahI
     */
    private String id;

    /**
     * 请求需要返回的结果行数，默认5，最大50
     * 注意：此参数可与 myRating 参数结合使用，但与 id 参数结合使用时不受支持。可接受的值包括1到50（含 0 和 5000）。默认值为 5。
     */
    private Integer maxResults;

    /**
     * 用于指示 API 返回可在指定国家/地区观看的视频的搜索结果。此参数值是 ISO 3166-1 alpha-2 国家/地区代码。
     * HK
     */
    private String regionCode;
}
