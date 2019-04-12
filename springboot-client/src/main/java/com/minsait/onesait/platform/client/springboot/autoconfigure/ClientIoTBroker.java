/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*******************************************************************************
 * Indra Sistemas, S.A.
 * 2013 - 2017  SPAIN
 * 
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.client.springboot.autoconfigure;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.client.TimeOutConfig;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

@Configuration
@EnableConfigurationProperties(ConnectionProperties.class)
public class ClientIoTBroker {

	private RestClient client;

	@Autowired
	private ConnectionProperties props;

	public RestClient init() throws SSAPConnectionException {
		if (client == null || !client.isConnected()) {
			client = new RestClient(props.getUrlRestIoTBroker(),
					TimeOutConfig.builder().connectTimeout(props.getConnectTimeoutInSec())
							.readTimeouts(props.getReadTimeoutInSec()).writeTimeout(props.getWriteTimeoutInSec())
							.timeunit(TimeUnit.SECONDS).build());
			client.connect(props.getToken(), props.getDeviceTemplate(), props.getDevice(), props.isSslverify());
		}
		return this.client;
	}

	public void reset() {
		if (client != null) {
			client.disconnect();
			client = null;
		}
	}
}
