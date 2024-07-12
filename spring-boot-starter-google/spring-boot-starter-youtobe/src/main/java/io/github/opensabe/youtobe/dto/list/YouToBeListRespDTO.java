package io.github.opensabe.youtobe.dto.list;

import io.github.opensabe.youtobe.dto.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * google data api 请求youtobe查询返回结果DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeListRespDTO {

    /**
     * 标识 API 资源类型。值为 youtube#searchListResponse。
     */
    private String kind;

    /**
     * 此资源的 Etag。
     */
    private String etag;

    /**
     * 可用作 pageToken 参数的值来检索结果集中的下一页的令牌。
     */
    private String nextPageToken;

    /**
     * 可用作 pageToken 参数的值来检索结果集中的上一页的令牌。
     */
    private String prevPageToken;

    /**
     * 搜索查询所用的地区代码。属性值是一个由两个字母构成的 ISO 国家/地区代码，用于标识区域。
     */
    private String regionCode;

    /**
     * 分页相关数据
     */
    private PageInfo pageInfo;

    /**
     * 与搜索条件匹配的结果列表。
     */
    private List<YouToBeListItemDTO> items;
}
