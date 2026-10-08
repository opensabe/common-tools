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
 * status 对象包含视频上传、处理和隐私状态等信息。
 * <p>
 * 详见：
 * <a href="https://developers.google.com/youtube/v3/docs/videos?hl=zh-cn#resource">videos resource</a>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeStatusDTO {

    /**
     * 上传视频的状态。
     * <p>
     * 有效值包括：deleted、failed、processed、rejected、uploaded
     */
    private String uploadStatus;

    /**
     * 说明无法上传视频的原因。仅当 {@link #uploadStatus} 为 failed 时才会出现此属性。
     */
    private String failureReason;

    /**
     * 说明上传视频被拒绝的原因。仅当 {@link #uploadStatus} 为 rejected 时才会出现此属性。
     */
    private String rejectionReason;

    /**
     * 视频的隐私状态。
     * <p>
     * 有效值包括：private、public、unlisted
     */
    private String privacyStatus;

    /**
     * 用于安排私享视频公开的日期和时间。仅当视频隐私状态为 private 时才能设置该值。
     * 该值采用 ISO 8601（YYYY-MM-DDThh:mm:ss.sZ）格式指定。
     */
    private String publishAt;

    /**
     * 视频的许可类型。
     * <p>
     * 有效值包括：creativeCommon、youtube
     */
    private String license;

    /**
     * 指明该视频是否可以嵌入到其他网站上。
     */
    private Boolean embeddable;

    /**
     * 指明视频的统计信息是否可供所有人查看。
     */
    private Boolean publicStatsViewable;

    /**
     * 指明视频是否适合儿童观看。
     */
    private Boolean madeForKids;

    /**
     * 在插入或更新视频时，此属性允许频道所有者指定视频是否适合儿童观看。
     */
    private Boolean selfDeclaredMadeForKids;

    /**
     * 指明视频是否包含经过修改或合成的内容（例如利用 AI 生成的内容）。
     */
    private Boolean containsSyntheticMedia;
}
