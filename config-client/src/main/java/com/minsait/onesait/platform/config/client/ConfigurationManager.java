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

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.client.exception.ConfigurationManagerException;
import com.minsait.onesait.platform.config.client.model.Configuration;
import com.minsait.onesait.platform.config.client.model.ConfigurationType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConfigurationManager {

	private static final String CONFIGURATION_REST = "/api/configurations";

	@Value("${onesaitplatform.api.rest.token}")
	String token;
	@Value("${onesaitplatform.api.rest.endpoint}")
	String endpoint;

	RestTemplate restTemplate = new RestTemplate();;
	ObjectMapper mapper = new ObjectMapper();

	public List<Configuration> getConfigurations() throws IOException, ConfigurationManagerException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-OP-APIKey", token);
		ResponseEntity<JsonNode> response = restTemplate.exchange(endpoint + CONFIGURATION_REST, HttpMethod.GET,
				new HttpEntity<>(headers), JsonNode.class);

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Error while retrieving configurations, status: {}", response.getStatusCodeValue());
			throw new ConfigurationManagerException(
					"Error while retrieving configurations, status: " + response.getStatusCodeValue());
		}

		final List<Configuration> configurations = mapper.readValue(response.getBody().toString(),
				new TypeReference<List<Configuration>>() {
				});
		return configurations;
	}

	public Configuration getConfigurationById(String id) throws IOException, ConfigurationManagerException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-OP-APIKey", token);
		ResponseEntity<JsonNode> response = restTemplate.exchange(endpoint + CONFIGURATION_REST + "/" + id,
				HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Error while retrieving configuration with id {}, status: {}", id, response.getStatusCodeValue());
			throw new ConfigurationManagerException("Error while retrieving configuration with id: " + id + ", status: "
					+ response.getStatusCodeValue());
		}
		final Configuration configuration = mapper.readValue(response.getBody().toString(), Configuration.class);
		return configuration;
	}

	public Configuration getConfigurationByIdentification(String identification)
			throws IOException, ConfigurationManagerException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-OP-APIKey", token);
		ResponseEntity<JsonNode> response = restTemplate.exchange(
				endpoint + CONFIGURATION_REST + "/identification/" + identification, HttpMethod.GET,
				new HttpEntity<>(headers), JsonNode.class);

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Error while retrieving configuration with identification {}, status: {}", identification,
					response.getStatusCodeValue());
			throw new ConfigurationManagerException("Error while retrieving configuration with identification: "
					+ identification + ", status: " + response.getStatusCodeValue());
		}
		final Configuration configuration = mapper.readValue(response.getBody().toString(), Configuration.class);
		return configuration;
	}

	public Configuration getConfiguration(String identification, String environment, ConfigurationType type)
			throws IOException, ConfigurationManagerException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-OP-APIKey", token);
		ResponseEntity<JsonNode> response = restTemplate
				.exchange(
						endpoint + CONFIGURATION_REST + "/" + identification + "/type/" + type.toString()
								+ "/environment/" + environment,
						HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.error(
					"Error while retrieving configuration with identification {} and type {} and environment {}, status: {}",
					identification, type, environment, response.getStatusCodeValue());
			throw new ConfigurationManagerException(
					"Error while retrieving configuration with identification: " + identification + " and environment: "
							+ environment + " and type: " + type + ", status: " + response.getStatusCodeValue());
		}
		final Configuration configuration = mapper.readValue(response.getBody().toString(), Configuration.class);
		return configuration;
	}

	public String createConfiguration(Configuration configuration) throws IOException, ConfigurationManagerException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-OP-APIKey", token);
		ResponseEntity<String> response = restTemplate.exchange(endpoint + CONFIGURATION_REST, HttpMethod.POST,
				new HttpEntity<>(configuration, headers), String.class);

		if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
			log.error("Error while creating configuration.", response.getStatusCodeValue());
			throw new ConfigurationManagerException(
					"Error while creating configuration. " + response.getStatusCodeValue());
		}

		return response.getBody();
	}

	public void updateConfiguration(Configuration configuration) throws IOException, ConfigurationManagerException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-OP-APIKey", token);
		ResponseEntity<String> response = restTemplate.exchange(endpoint + CONFIGURATION_REST, HttpMethod.PUT,
				new HttpEntity<>(configuration, headers), String.class);

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Error while updating configuration.", response.getStatusCodeValue());
			throw new ConfigurationManagerException(
					"Error while updating configuration. " + response.getStatusCodeValue());
		}
	}

	public void deleteConfigurationById(String id) throws IOException, ConfigurationManagerException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-OP-APIKey", token);
		ResponseEntity<String> response = restTemplate.exchange(endpoint + CONFIGURATION_REST + "/" + id,
				HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Error while deleting configuration.", response.getStatusCodeValue());
			throw new ConfigurationManagerException(
					"Error while deleting configuration. " + response.getStatusCodeValue());
		}
	}

	public void deleteConfiguration(String identification, String environment, ConfigurationType type)
			throws IOException, ConfigurationManagerException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-OP-APIKey", token);
		ResponseEntity<String> response = restTemplate
				.exchange(
						endpoint + CONFIGURATION_REST + "/" + identification + "/type/" + type.toString()
								+ "/environment/" + environment,
						HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Error while deleting configuration.", response.getStatusCodeValue());
			throw new ConfigurationManagerException(
					"Error while deleting configuration. " + response.getStatusCodeValue());
		}
	}
}
