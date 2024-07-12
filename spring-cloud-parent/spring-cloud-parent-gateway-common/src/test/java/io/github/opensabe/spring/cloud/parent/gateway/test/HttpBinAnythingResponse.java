package io.github.opensabe.spring.cloud.parent.gateway.test;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
public class HttpBinAnythingResponse {
    private Map<String, List<String>> args;
    private String data;
    private Map<String, List<String>> form;
    private Map<String, List<String>> headers;
    private String method;
    private String origin;
    private String url;
}
