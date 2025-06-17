package io.github.opensabe.youtobe.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "google.youtobe.api")
@Data
public class YouToBeDataApiProperties {

    /**
     * 谷歌提供的youtobe的 search api
     */
    private String search;

    /**
     * 谷歌提供的youtobe的 list api
     */
    @NotBlank
    private String list;

    /**
     * 设置视频所在区域
     */
    private String regionCode;

    /**
     * 谷歌授权的api key
     */
    @NotEmpty
    private List<String> keys;
}
