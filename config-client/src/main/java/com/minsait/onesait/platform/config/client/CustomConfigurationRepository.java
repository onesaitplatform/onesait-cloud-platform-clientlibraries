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
package com.minsait.onesait.platform.config.client;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.client.encryptor.JasyptConfig;
import com.minsait.onesait.platform.config.client.model.Configuration;
import com.minsait.onesait.platform.config.client.model.SpringCloudPlatformConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(value = "onesaitplatform.spring.cloud.config.enabled", havingValue = "true", matchIfMissing = false)
public class CustomConfigurationRepository implements EnvironmentRepository, Ordered {

	@Autowired
	ConfigurationManager configManager;

	@Value("${onesaitplatform.spring.cloud.config.identification}")
	String bpoConfig;

	@Override
	public Environment findOne(String application, String profile, String label) {
		Environment environment = new Environment(application, StringUtils.commaDelimitedListToStringArray(profile),
				label, null, null);
		ObjectMapper mapper = new ObjectMapper();
		log.info("CustomConfigurationRepository loading. Application: {} - Profile: {} - label: {}", application, profile, label);
		try {
			Configuration configuration = configManager.getConfigurationByIdentification(bpoConfig);
			log.info("Platform configuration getting correctly: {}", configuration.getIdentification()
					);
			List<SpringCloudPlatformConfiguration> platformConfig = mapper.readValue(configuration.getYml(),
					new TypeReference<List<SpringCloudPlatformConfiguration>>() {
					});

			SpringCloudPlatformConfiguration app = platformConfig.stream()
					.filter(aps -> aps.getIdentification().equals(application)).findFirst().get();
			if (app != null) {
				for (Map.Entry<String, String> entry : app.getProperties().entrySet()) {
					if (entry.getValue().startsWith("ENC("))
						app.getProperties().put(entry.getKey(),
								JasyptConfig.getEncryptor().decrypt(encryptedProperty(entry.getValue())));
				}
				;
			}
			environment.add(new PropertySource("classpath:/application.yml", app.getProperties()));
			log.info("Spring Cloud Environment created successfully.");
			return environment;
		} catch (Exception e) {
			log.error("Error getting BPO configuration.", e);
			return null;
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	public String encryptedProperty(String encrypted) {
		final Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(encrypted);
		while (m.find()) {
			return m.group(1);
		}
		return encrypted;
	}
}
