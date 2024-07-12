package io.github.opensabe.youtobe.dto.search;

import io.github.opensabe.youtobe.dto.YouToBeSnippetDTO;
import io.github.opensabe.youtobe.dto.YouToBeVideoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * youtobe data api 接口返回item DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeSearchItemDTO {

    /**
     * 如果是视频类型，这就是视频kind和videoId
     */
    private YouToBeVideoDTO id;

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
}
