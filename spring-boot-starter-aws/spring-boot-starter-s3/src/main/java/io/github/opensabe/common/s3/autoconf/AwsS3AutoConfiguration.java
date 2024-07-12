package io.github.opensabe.common.s3.autoconf;

import io.github.opensabe.common.s3.configuration.AwsS3Configuration;
import io.github.opensabe.common.s3.properties.S3Properties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

//https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#changes-to-auto-configuration

@Import({AwsS3Configuration.class})
//@Configuration(proxyBeanMethods = false)
@AutoConfiguration
@EnableConfigurationProperties(S3Properties.class)
public class AwsS3AutoConfiguration {

}
