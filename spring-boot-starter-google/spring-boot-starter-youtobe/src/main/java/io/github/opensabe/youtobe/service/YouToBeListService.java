package io.github.opensabe.youtobe.service;

import io.github.opensabe.common.core.AppException;
import io.github.opensabe.common.core.ErrorCode;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.youtobe.dto.list.YouToBeListReqDTO;
import io.github.opensabe.youtobe.dto.list.YouToBeListRespDTO;
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
 * youtobe list通用service
 * https://developers.google.com/youtube/v3/docs/videos/list?hl=zh-cn
 */
@Service
@Log4j2
public class YouToBeListService {

    @Autowired
    private YouToBeDataApiProperties properties;

    private final OkHttpClient okHttpClient;

    @Autowired
    public YouToBeListService(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public YouToBeListRespDTO getList(YouToBeListReqDTO reqDTO) throws AppException {
        if (Objects.isNull(properties) || Objects.isNull(properties.getList()) || CollectionUtils.isEmpty(properties.getKeys())) {
            throw new AppException(ErrorCode.INVALID, "api or key not null");
        }

        // 拼接get方式请求入参
        String url = this.buildUrlAndParam(properties, reqDTO);
        log.info("YouToBeListService.getList url:{}", url);

        Request request = new Request.Builder().url(url)
//                .addHeader("Content-Type", "text/json; charset=utf-8")
                .build();

        // remote request api
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            log.error("YouToBeListService.getList error:{}", e);
            return null;
        }

        // analysis
        YouToBeListRespDTO result = null;
        try {
//            if (200 == response.code()) {
                result = JsonUtil.parseObject(response.body().string(), YouToBeListRespDTO.class);
//            } else {
//                log.warn("YouToBeListService.getList google data api http code:{}", response.code());
//            }
        } catch (Exception e) {
            log.error("YouToBeListService.getList json error:{}", e);
            return null;
        }

        log.info("YouToBeListService.getList response.code:{}, result:{}", response.code(), JsonUtil.toJSONString(result));

        return result;
    }

    /**
     * 根据请求入参，拼凑请求url，这里是get请求方式的参数拼接
     *
     * @param properties api原始地址和key
     * @param reqDTO     请求入参
     * @return
     */
    private String buildUrlAndParam(YouToBeDataApiProperties properties, YouToBeListReqDTO reqDTO) {
        StringBuilder sb = new StringBuilder(properties.getList());
        // 由于可能有多个key，这里随机取一个使用
        Random random = new Random();
        int randomIndex = random.nextInt(properties.getKeys().size());
        sb.append("?key=").append(properties.getKeys().get(randomIndex));

        // 这里固定请求的part为snippet,contentDetails,statistics
        sb.append("&part=snippet,contentDetails,statistics");

        if (StringUtils.isNotBlank(reqDTO.getId())) {
            sb.append("&id=").append(reqDTO.getId());
        }
        if (Objects.nonNull(reqDTO.getMaxResults())) {
            sb.append("&maxResults=").append(reqDTO.getMaxResults());
        }
        if (StringUtils.isNotBlank(reqDTO.getRegionCode())) {
            sb.append("&regionCode=").append(reqDTO.getRegionCode());
        } else {
            sb.append("&regionCode=").append(properties.getRegionCode());
        }

        return sb.toString();
    }
}
