package io.github.opensabe.youtobe.dto.list;

import io.github.opensabe.youtobe.dto.YouToBeContentDetailsDTO;
import io.github.opensabe.youtobe.dto.YouToBeSnippetDTO;
import io.github.opensabe.youtobe.dto.YouToBeStatisticsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * youtobe data api 接口返回item DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeListItemDTO {

    /**
     * videoId
     */
    private String id;

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

    private YouToBeContentDetailsDTO contentDetails;

    private YouToBeStatisticsDTO statistics;
}
