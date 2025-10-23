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
 * contentDetails 对象包含视频内容的相关信息，包括视频时长以及关于视频是否有字幕的指示。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeContentDetailsDTO {

    /**
     * 视频的长度。属性值是 ISO 8601 时长。例如，对于时长至少为一分钟且时长不到 1 小时的视频，时长的格式为 PT#M#S，其中字母 PT 表示相应的值表示时间段，字母 M 和 S 分别表示时长（以分钟和秒为单位）。M 和 S 字母前面的 # 字符都是整数，用于指定视频的分钟数（或秒数）。例如，值 PT15M33S 表示视频时长为 15 分 33 秒。
     * <p>
     * 如果视频时长至少为 1 小时，则时长格式为 PT#H#M#S，其中字母 H 前面的 # 指定了视频的时长（以小时为单位），其他所有详细信息与上文所述相同。如果视频时长至少为一天，则字母 P 和 T 会分隔，且值的格式为 P#DT#H#M#S。如需了解完整详情，请参阅 ISO 8601 规范。
     */
    private String duration;

    /**
     * 指明视频支持 3D 还是 2D。
     */
    private String dimension;

    /**
     * 指明视频是能够以高清 (HD) 模式播放，还是仅能以标清模式播放。
     * <p>
     * 此属性的有效值包括：
     * hd
     * sd
     */
    private String definition;

    /**
     * 指明视频是否提供字幕。
     * <p>
     * 此属性的有效值包括：
     * false
     * true
     */
    private String caption;

    /**
     * 指明视频是否代表许可内容，即内容已上传到与 YouTube 内容合作伙伴关联的频道，并由该合作伙伴提出版权主张。
     */
    private Boolean licensedContent;

    /**
     * 指定视频的投影格式。
     * <p>
     * 此属性的有效值包括：
     * 360
     * rectangular
     */
    private String projection;

    private Object contentRating;
}
