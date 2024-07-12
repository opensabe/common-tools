package io.github.opensabe.youtobe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * youtobe data api 接口返回item中Video DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YouToBeVideoDTO {

    /**
     * 视频Id
     */
    private String videoId;

    /**
     * 标识 API 资源类型。值为 youtube#video。
     */
    private String kind;
}
