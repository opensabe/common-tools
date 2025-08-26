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
package io.github.opensabe.common.s3.autoconf;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import io.github.opensabe.common.s3.configuration.AwsS3Configuration;
import io.github.opensabe.common.s3.configuration.SpringDataS3ConverterConfig;
import io.github.opensabe.common.s3.properties.S3Properties;

//https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#changes-to-auto-configuration

@Import({AwsS3Configuration.class, SpringDataS3ConverterConfig.class})
@AutoConfiguration
@EnableConfigurationProperties(S3Properties.class)
public class AwsS3AutoConfiguration {

}
