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

import java.util.List;

/**
 * youtobe data api 接口返回snippet DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeSnippetDTO {

    /**
     * 资源发布时间
     */
    private String publishTime;

    /**
     * 资源发布时间
     */
    private String publishedAt;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 资源标题
     */
    private String title;

    /**
     * 资源channelId
     */
    private String channelId;

    /**
     * 资源channel标题
     */
    private String channelTitle;

    /**
     * liveBroadcastContent
     */
    private String liveBroadcastContent;

    /**
     * 图片资源
     * 起初是YouToBeSnippetThumbnailDTO，直接改成String，因为返回的是个JSON
     */
    private String thumbnails;

    /**
     * video的tags，是个字符串数组
     */
    private List<String> tags;
}
