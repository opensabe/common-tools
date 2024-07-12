package io.github.opensabe.common.secret;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterSecretStringResult {
    private final boolean foundSensitiveString;
    private final String filteredContent;
}
