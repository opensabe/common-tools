package io.github.opensabe.youtobe.dto.search;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * google data api 请求youtobe查询入参DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeSearchReqDTO {

    /**
     * 核心：请求内容
     */
    private String q;

    /**
     * youtobe的渠道Id，非必填
     */
//    private String channelId;

    /**
     * 请求需要返回的结果行数，默认10，最大50
     */
    private Integer maxResults = 10;

    /**
     * 请求资源的类型
     */
//    private SearchTypeEnum type;

    /**
     * 用于指示 API 返回可在指定国家/地区观看的视频的搜索结果。此参数值是 ISO 3166-1 alpha-2 国家/地区代码。
     * HK
     */
    private String regionCode;
}
