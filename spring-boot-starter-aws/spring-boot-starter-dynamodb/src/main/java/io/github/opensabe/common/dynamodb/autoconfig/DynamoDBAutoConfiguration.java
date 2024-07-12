package io.github.opensabe.common.dynamodb.autoconfig;

import io.github.opensabe.common.dynamodb.config.DynamoDBConfiguration;
import io.github.opensabe.common.dynamodb.configuration.MonitorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({DynamoDBConfiguration.class, MonitorConfiguration.class})
public class DynamoDBAutoConfiguration {
}
