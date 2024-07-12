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
