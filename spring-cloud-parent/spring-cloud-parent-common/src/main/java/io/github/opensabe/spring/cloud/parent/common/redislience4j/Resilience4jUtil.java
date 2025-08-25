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
package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Resilience4jUtil {
	public static String getServiceInstance(URL url) {
		return getServiceInstance(url.getHost(), url.getPort());
	}

	public static String getServiceInstance(String host, int port) {
		return host + ":" + port;
	}

	public static String getServiceInstanceMethodId(URL url, Method method) {
		return getServiceInstance(url) + ":" + method.toGenericString();
	}

	public static String getServiceInstanceMethodId(String host, int port, Method method) {
		return getServiceInstance(host, port) + ":" + method.toGenericString();
	}

	public static String getServiceInstanceMethodId(String host, int port, String path) {
		String[] split = StringUtils.split(path, "/");
		String suffix = split != null
				? "/" + Arrays.stream(split).filter(Resilience4jUtil::shouldNotBeIgnored).limit(3).collect(Collectors.joining("/"))
				: "/";
		return getServiceInstance(host, port) + suffix;
	}

	private static final Pattern ID = Pattern.compile("[0-9]{12}");

	private static boolean shouldNotBeIgnored(String split) {
		if (ID.matcher(split).find()) {
			return false;
		}
		return true;
	}
}
