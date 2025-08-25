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
 * statistics 对象包含视频的相关统计信息。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeStatisticsDTO {

    /**
     * 视频的观看次数。
     */
    private Long viewCount;

    /**
     * 已表示喜欢该视频的用户数量。
     */
    private Long likeCount;

    /**
     * 表示不喜欢视频的用户数量。
     */
    private Long dislikeCount;

    /**
     * 视频的评论数。
     */
    private Long commentCount;
}
