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
package io.github.opensabe.youtobe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * youtobe data api 接口返回数据分页相关DTO
 * pageInfo 对象用于封装结果集的分页信息。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo {

    /**
     * 结果集中的结果总数。请注意，该值为近似值，并不代表确切值。此外，最大值为 1,000,000。
     */
    private Integer totalResults;

    /**
     * API 响应中包含的结果数量。
     */
    private Integer resultsPerPage;
}
