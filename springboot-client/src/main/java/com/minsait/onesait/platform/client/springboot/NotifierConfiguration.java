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
package com.minsait.onesait.platform.client.springboot;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.client.springboot.autoconfigure.NotifierClientConnection;
import com.minsait.onesait.platform.client.springboot.proxy.PostProcessor;

@Configuration
@ConditionalOnProperty(value = "onesaitplatform.notifierclient.enabled", havingValue = "true", matchIfMissing = false)
public class NotifierConfiguration {

	@Value("${onesaitplatform.notifierclient.server}")
	private String server;

	@Value("${onesaitplatform.notifierclient.username}")
	private String username;

	@Value("${onesaitplatform.notifierclient.password}")
	private String password;

	@Value("${onesaitplatform.notifierclient.apikey:#{null}}")
	private String apikey;

	@Value("${onesaitplatform.notifierclient.sslverify:false}")
	private boolean sslverify;

	@Autowired
	private PostProcessor postProcessor;

	@Bean("NotifierClientConnection")
	public NotifierClientConnection setUpNotifierConnection() {
		NotifierClientConnection connection = new NotifierClientConnection();
		if (apikey != null) {
			connection.setConnectionParameters(server, apikey, sslverify);
			return connection;
		}
		connection.setConnectionParameters(server, username, password, sslverify);

		return connection;
	}

	@PostConstruct
	public void createOrUpdateEntity() {
		for (Class<?> entity : postProcessor.getClassEntities()) {
			setUpNotifierConnection().init().createOrUpdateOntology(entity);
		}
	}
}
