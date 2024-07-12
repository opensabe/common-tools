package io.github.opensabe.spring.cloud.starter.third.client.test;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Data
public class HttpBinAnythingResponse {
    private Map<String, String> args;
    private String data;
    private Map<String, String> form;
    private Map<String, String> headers;
    private String method;
    private String origin;
    private String url;
}
