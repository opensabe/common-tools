package io.github.opensabe.apple;

import io.github.opensabe.apple.appstoreconnectapi.AppleStoreConnectConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AppleInPurchaseConfiguration.class, AppleStoreConnectConfiguration.class, AppleLoginConfiguration.class})
public class AppleAutoConfiguration {
}
