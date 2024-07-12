package io.github.opensabe.common.elasticsearch.script;


import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class AbstractElasticSearchScript {
    protected abstract String file();

    private String script;

    @PostConstruct
    private void init() throws IOException {
        try (InputStream resourceAsStream = AbstractElasticSearchScript.class.getResourceAsStream(file())) {
            this.script = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public String getScript(Object... objects) {
        return String.format(script, objects);
    }
}
