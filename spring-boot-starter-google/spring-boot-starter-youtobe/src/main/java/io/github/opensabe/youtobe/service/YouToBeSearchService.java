package io.github.opensabe.youtobe.service;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.cloud.parent.common.handler.FrontendException;
import io.github.opensabe.youtobe.dto.search.YouToBeSearchReqDTO;
import io.github.opensabe.youtobe.dto.search.YouToBeSearchRespDTO;
import io.github.opensabe.youtobe.properties.YouToBeDataApiProperties;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

/**
 * youtobe search通用service
 * <a href="https://developers.google.com/youtube/v3/docs/search/list?hl=zh-cn">...</a>
 */
@Service
@Log4j2
public class YouToBeSearchService {

    private final YouToBeDataApiProperties properties;

    private final OkHttpClient okHttpClient;

    @Autowired
    public YouToBeSearchService(YouToBeDataApiProperties properties, OkHttpClient okHttpClient) {
        this.properties = properties;
        this.okHttpClient = okHttpClient;
    }

    public YouToBeSearchRespDTO getSearch(YouToBeSearchReqDTO reqDTO)  {
        if (Objects.isNull(properties) || Objects.isNull(properties.getSearch()) || CollectionUtils.isEmpty(properties.getKeys())) {
            throw new FrontendException(BizCodeEnum.INVALID, "api or key not null");
        }

        // 拼接get方式请求入参
        String url = this.buildUrlAndParam(properties, reqDTO);
        log.info("YouToBeSearchService.getSearch url:{}", url);
        Request request = new Request.Builder().url(url).build();

        // remote request api
        try(Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new FrontendException(BizCodeEnum.FAIL, response.message());
            }
            String body = Objects.requireNonNull(response.body()).string();
            log.info("YouToBeSearchService.getSearch response.code:{}, result:{}", response.code(), body);

            return JsonUtil.parseObject(body, YouToBeSearchRespDTO.class);
        } catch (IOException e) {
            log.error("YouToBeSearchService.getSearch error", e);
            return null;
        }

    }

    /**
     * 根据请求入参，拼凑请求url，这里是get请求方式的参数拼接
     *
     * @param properties api原始地址和key
     * @param reqDTO     请求入参
     */
    private String buildUrlAndParam(YouToBeDataApiProperties properties, YouToBeSearchReqDTO reqDTO) {
        StringBuilder sb = new StringBuilder(properties.getSearch());
        // 由于可能有多个key，这里随机取一个使用
        Random random = new Random();
        int randomIndex = random.nextInt(properties.getKeys().size());
        sb.append("?key=").append(properties.getKeys().get(randomIndex));

        // 这里固定请求的part为snippet
        sb.append("&part=snippet");

        if (StringUtils.isNotBlank(reqDTO.getQ())) {
            // 不能出现&号，不然url就截断了
            if (reqDTO.getQ().contains("&")) {
                reqDTO.setQ(reqDTO.getQ().replace("&", ""));
            }
            sb.append("&q=").append(reqDTO.getQ());
        }
        if (Objects.nonNull(reqDTO.getMaxResults())) {
            sb.append("&maxResults=").append(reqDTO.getMaxResults());
        }
//        if (StringUtils.isNotBlank(reqDTO.getChannelId())) {
//            sb.append("&channelId=").append(reqDTO.getChannelId());
//        }
//        if (Objects.nonNull(reqDTO.getType())) {
//            sb.append("&type=").append(reqDTO.getType().getName());
//        }
        if (StringUtils.isNotBlank(reqDTO.getRegionCode())) {
            sb.append("&regionCode=").append(reqDTO.getRegionCode());
        } else {
            sb.append("&regionCode=").append(properties.getRegionCode());
        }

        return sb.toString();
    }
}
