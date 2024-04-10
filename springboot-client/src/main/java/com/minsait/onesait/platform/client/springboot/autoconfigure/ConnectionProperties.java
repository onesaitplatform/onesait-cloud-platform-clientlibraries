/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties
@Data
public class ConnectionProperties {

	@Value("${onesaitplatform.iotclient.urlRestIoTBroker:http://localhost:1900/iot-broker/}")
	private String urlRestIoTBroker;

	@Value("${onesaitplatform.iotclient.urlRestControlpanel:http://controlpanelservice.onesait-platform:18000}")
	private String urlRestControlpanel;

	@Value("${onesaitplatform.iotclient.token:null}")
	private String token;

	@Value("${onesaitplatform.iotclient.multitenant:false}")
	private boolean multitenant;

	@Value("${onesaitplatform.iotclient.admin-api-token:null}")
	private String adminAPIToken;

	@Value("${onesaitplatform.iotclient.deviceTemplate}")
	private String deviceTemplate;

	@Value("${onesaitplatform.iotclient.device}")
	private String device;

	@Value("${onesaitplatform.iotclient.sslverify:false}")
	private boolean sslverify;

	@Value("${onesaitplatform.iotclient.connectTimeoutInSec:10}")
	private int connectTimeoutInSec;
	@Value("${onesaitplatform.iotclient.writeTimeoutInSec:10}")
	private int writeTimeoutInSec;
	@Value("${onesaitplatform.iotclient.readTimeoutInSec:10}")
	private int readTimeoutInSec;

}
