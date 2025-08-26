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
 * youtobe data api 接口返回snippet中资源缩略图（含多个分辨率）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeSnippetThumbnailDTO {

    /**
     * 默认图：default为保留字，这里用Default
     */
    private ImgInfo Default;

    /**
     * 高清图
     */
    private ImgInfo high;

    /**
     * 标准图
     */
    private ImgInfo medium;

    @Data
    class ImgInfo {
        /**
         * 缩略图地址
         */
        private String url;

        /**
         * 缩略图尺寸：宽
         */
        private Integer width;

        /**
         * 缩略图尺寸：高
         */
        private Integer height;
    }
}
