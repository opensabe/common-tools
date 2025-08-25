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

import io.github.opensabe.youtobe.dto.YouToBeSnippetDTO;
import io.github.opensabe.youtobe.dto.YouToBeVideoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * youtobe data api 接口返回item DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeSearchItemDTO {

    /**
     * 如果是视频类型，这就是视频kind和videoId
     */
    private YouToBeVideoDTO id;

    /**
     * 标识 API 资源类型。值为 youtube#searchListResponse。
     */
    private String kind;

    /**
     * 此资源的 Etag。
     */
    private String etag;

    /**
     * snippet数据
     */
    private YouToBeSnippetDTO snippet;
}
