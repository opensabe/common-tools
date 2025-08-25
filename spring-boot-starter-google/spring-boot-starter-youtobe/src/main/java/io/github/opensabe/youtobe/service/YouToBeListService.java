/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.youtobe.service;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.cloud.parent.common.handler.FrontendException;
import io.github.opensabe.youtobe.dto.list.YouToBeListReqDTO;
import io.github.opensabe.youtobe.dto.list.YouToBeListRespDTO;
import io.github.opensabe.youtobe.properties.YouToBeDataApiProperties;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * youtobe list通用service
 * <a href="https://developers.google.com/youtube/v3/docs/videos/list?hl=zh-cn">...</a>
 */
@Service
@Log4j2
public class YouToBeListService {

    private final YouToBeDataApiProperties properties;

    private final OkHttpClient okHttpClient;

    @Autowired
    public YouToBeListService(YouToBeDataApiProperties properties, OkHttpClient okHttpClient) {
        this.properties = properties;
        this.okHttpClient = okHttpClient;
    }

    public YouToBeListRespDTO getList(YouToBeListReqDTO reqDTO) {
        if (Objects.isNull(properties) || Objects.isNull(properties.getList()) || CollectionUtils.isEmpty(properties.getKeys())) {
            throw new FrontendException(BizCodeEnum.INVALID, "api or key not null");
        }

        // 拼接get方式请求入参
        String url = this.buildUrlAndParam(properties, reqDTO);
        log.info("YouToBeListService.getList url:{}", url);

        Request request = new Request.Builder().url(url)
//                .addHeader("Content-Type", "text/json; charset=utf-8")
                .build();

        // remote request api
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new FrontendException(BizCodeEnum.FAIL, response.message());
            }
            // analysis
            String body = Objects.requireNonNull(response.body()).string();

            log.info("YouToBeListService.getList response.code:{}, result:{}", response.code(), body);

            return JsonUtil.parseObject(body, YouToBeListRespDTO.class);
        } catch (IOException e) {
            log.error("YouToBeListService.getList error", e);
            return null;
        }
    }

    /**
     * 根据请求入参，拼凑请求url，这里是get请求方式的参数拼接
     *
     * @param properties api原始地址和key
     * @param reqDTO     请求入参
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
