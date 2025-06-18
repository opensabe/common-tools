package io.github.opensabe.youtobe.service;


import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.youtobe.App;
import io.github.opensabe.youtobe.dto.list.YouToBeListReqDTO;
import io.github.opensabe.youtobe.dto.list.YouToBeListRespDTO;
import io.github.opensabe.youtobe.dto.search.YouToBeSearchReqDTO;
import io.github.opensabe.youtobe.dto.search.YouToBeSearchRespDTO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

//todo 在 github action 里面加入 secret，之后通过环境变量读取
@Disabled
@SpringBootTest(classes = App.class)
public class YouToBeTest {

    private static final String key = "${key}";

    private static final String channelId = "${channelId}";

    private static final String url = "https://www.googleapis.com/youtube/v3/search?key=" + key + "&part=snippet&maxResults=10&q=Highlights&channelId=" + channelId;

    private OkHttpClient okHttpClient;

    @Autowired
    private YouToBeSearchService youToBeSearchService;

    @Autowired
    private YouToBeListService youToBeListService;

    @Test
    public void getTest() throws IOException {
        okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).addHeader("Content-Type", "text/json; charset=utf-8").build();

        Response response = okHttpClient.newCall(request).execute();

        YouToBeSearchRespDTO result = JsonUtil.parseObject(response.body().string(), YouToBeSearchRespDTO.class);

        System.out.println(JsonUtil.toJSONString(result));
    }

    /**
     * search 方法单测
     *
     */
    @Test
    public void getSearchTest() {
        YouToBeSearchReqDTO reqDTO = new YouToBeSearchReqDTO();
        reqDTO.setQ("West Adelaide Bearcats 60 - 66 South Adelaide Panthers | Highlights");
        reqDTO.setMaxResults(25);
        reqDTO.setRegionCode("HK");
        YouToBeSearchRespDTO result = youToBeSearchService.getSearch(reqDTO);
        System.out.println(JsonUtil.toJSONString(result));
    }

    /**
     * list 方法单测
     *
     */
    @Test
    public void getListTest() {
        YouToBeListReqDTO reqDTO = new YouToBeListReqDTO();
        reqDTO.setId("Ks-_Mh1QhMc,c0KYU2j0TM4,eIho2S0ZahI");
        reqDTO.setRegionCode("HK");
        YouToBeListRespDTO result = youToBeListService.getList(reqDTO);
        System.out.println(JsonUtil.toJSONString(result));
    }
}
