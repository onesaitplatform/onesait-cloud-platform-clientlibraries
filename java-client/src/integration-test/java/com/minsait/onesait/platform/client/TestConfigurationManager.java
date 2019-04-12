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
package com.minsait.onesait.platform.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.yaml.snakeyaml.Yaml;

import com.minsait.onesait.platform.client.enums.ConfigurationType;
import com.minsait.onesait.platform.client.exception.ConfigurationManagerException;
import com.minsait.onesait.platform.client.model.Configuration;
import com.minsait.onesait.platform.testing.IntegrationTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Category(IntegrationTest.class)
public class TestConfigurationManager {

	private final static String USERNAME = "administrator";
	private final static String PASSWORD = "Changed!";
	private final static String SERVER = "http://localhost:18000/controlpanel/";
	private final static String REALM = "NewRealm";
	private final static String ENVIRONMENT = "dev";
	private final static String YML_SAMPLE = "rancher:\r\n  url: https://rancher.sofia4cities.com/\r\n  accessKey: 471D90A16431C2CE7158\r\n  secretKey: VSoq31eGBqaFsG4vUcuvdpkQL1T64FypVPVckaDx";
	private final static ConfigurationType TYPE = ConfigurationType.RANCHER;
	private static ConfigurationManager manager;

	@BeforeClass
	public static void startUp() throws ConfigurationManagerException {
		log.info("Initializing Manager");
		manager = new ConfigurationManager(USERNAME, PASSWORD, SERVER);
	}

	@Test
	public void getAllConfigurations() throws IOException, ConfigurationManagerException {
		log.info("Getting Configurations from open platform");
		final List<Configuration> configurations = manager.getConfigurations();
		Assert.assertTrue(configurations.size() > 0);
	}

	@Test
	public void configurationCRUD() throws IOException, ConfigurationManagerException {
		final Yaml yaml = new Yaml();

		@SuppressWarnings("unchecked")
		final Configuration configuration = Configuration.builder().description("Rancher config sample")
				.environment(ENVIRONMENT).suffix(REALM).username(USERNAME).type(TYPE)
				.yml((Map<String, Object>) yaml.load(YML_SAMPLE)).build();
		log.info("Creating configuration");
		final String id = manager.createConfiguration(configuration);
		Assert.assertNotNull(id);
		log.info("Created configuration, id is {}", id);
		Configuration retrievedConfig = manager.getConfigurationById(id);
		Assert.assertNotNull(retrievedConfig);
		log.info("Retrieved config from the platform by id");
		retrievedConfig = manager.getConfiguration(ENVIRONMENT, REALM, TYPE);
		Assert.assertNotNull(retrievedConfig);
		log.info("Retrieved config from the platform by parameters");

		configuration.setDescription("This is a new description");
		log.info("Updating configuration with new description and id {}", id);
		manager.updateConfiguration(configuration, id);

		log.info("Deleting configuration with id {}", id);
		manager.deleteConfiguration(id);

	}
}
