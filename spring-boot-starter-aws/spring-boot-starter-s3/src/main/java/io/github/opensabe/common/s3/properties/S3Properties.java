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
package io.github.opensabe.common.s3.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = S3Properties.PREFIX)
public class S3Properties {
	
	public static final String PREFIX = "aws.s3";

	private Boolean enabled = true;

	/**
	 * 供S3ClientWrapper使用，为了兼容 common-s3-wrapper 使用的
	 */
	private String folderName;

	/**
	 * 需要配置，否则会从 common config 读取 s3.access_key.id
	 */
	private String accessKeyId;
	/**
	 * 需要配置，否则会从 common config 读取 s3.access_key.secret_key
	 */
	private String accessKey;
	/**
	 * 默认的桶
	 */
	private String defaultBucket;
	/**
	 * 处于的 region
	 */
	private String region;

	/**
	 * 代理配置，一般本地启动需要挂全局代理
	 */
	private String endpoint;
	/**
	 * 供 AsyncTaskFileService 使用
	 */
	private String profile;
	/**
	 * 供 AsyncTaskFileService 使用
	 */
	private String staticDomain;
}
