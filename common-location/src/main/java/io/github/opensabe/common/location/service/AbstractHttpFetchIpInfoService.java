package io.github.opensabe.common.location.service;

import io.github.opensabe.common.location.vo.IpLocation;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractHttpFetchIpInfoService<T> {
    protected final RestTemplate restTemplate = new RestTemplate();

    protected abstract Class<T> clazz();

    protected abstract String url(String ip);

    protected abstract HttpHeaders httpHeaders();

    protected abstract HttpMethod httpMethod();

    protected abstract IpLocation transfer(T t);

    public IpLocation getIpLocation(String ip) {
        String url = url(ip);
        HttpHeaders httpHeaders = httpHeaders();
        HttpMethod httpMethod = httpMethod();
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<T> exchange = restTemplate.exchange(url, httpMethod, httpEntity, clazz());
        return transfer(exchange.getBody());
    }
}
