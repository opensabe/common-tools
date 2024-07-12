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
