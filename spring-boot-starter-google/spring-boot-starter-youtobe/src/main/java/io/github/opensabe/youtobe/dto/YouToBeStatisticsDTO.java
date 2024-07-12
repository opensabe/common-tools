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
