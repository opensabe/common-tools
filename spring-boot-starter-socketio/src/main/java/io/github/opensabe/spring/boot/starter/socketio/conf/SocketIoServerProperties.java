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
package io.github.opensabe.spring.boot.starter.socketio.conf;

import com.corundumstudio.socketio.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(SocketIoServerProperties.PREFIX)
public class SocketIoServerProperties extends Configuration {
	public static final String PREFIX = "server.socketio";
	//用来隔离 Store 订阅 topic 名称的，保证同一个 redis 上不同微服务不会收到来自其他微服务的消息
	private String nameSpace = "socket-io";

	private String healthCheckPacketName;

	public String getHealthCheckPacketName() {
		return healthCheckPacketName;
	}

	public void setHealthCheckPacketName(String healthCheckPacketName) {
		this.healthCheckPacketName = healthCheckPacketName;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}


}
