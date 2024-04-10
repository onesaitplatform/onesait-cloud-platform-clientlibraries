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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.client.TimeOutConfig;
import com.minsait.onesait.platform.client.springboot.dto.DeviceTokenDTO;
import com.minsait.onesait.platform.client.springboot.util.SSLUtil;

import lombok.Getter;

@Configuration
@EnableConfigurationProperties(ConnectionProperties.class)
public class ClientIoTBroker {

	private RestClient client;

	private final Map<String, RestClient> clients = new HashMap<>();

	@Getter
	public Collection<DeviceTokenDTO> tokens = new ArrayList<>();

	@Autowired
	private ConnectionProperties props;

	private static final String TOKENS_API_PATH = "/controlpanel/api/devices/%s/token";
	private static final String HEADER_NAME = "X-OP-APIKey";

	public RestClient init(String tenant) {
		if (StringUtils.isEmpty(tenant) || !props.isMultitenant()) {
			Assert.notNull(client, "No client configured");
			return client;
		} else {
			final RestClient c = clients.get(tenant);
			Assert.notNull(c, "No client found for tenant " + tenant);
			return c;
		}
	}

	public void reset(String tenant) {
		if (tenant == null) {
			client.disconnect();
			client = null;
		} else {
			clients.get(tenant).disconnect();
			clients.remove(tenant);
		}

	}

	@PostConstruct
	private void fetchDeviceTokens() throws URISyntaxException, UnsupportedEncodingException {
		if (props.isMultitenant()) {
			String apiEndpoint = null;
			final String brokerUrl = props.getUrlRestIoTBroker();
			final URI uri = new URI(brokerUrl);
			if ("localhost".equalsIgnoreCase(uri.getHost())) {
				apiEndpoint = uri.getScheme() + "://" + uri.getHost() + ":18000" + String.format(TOKENS_API_PATH,
						URLEncoder.encode(props.getDeviceTemplate(), StandardCharsets.UTF_8.name()));
			} else if(!uri.getHost().contains("iotbrokerservice")){
				apiEndpoint = uri.getScheme() + "://" + uri.getHost() + String.format(TOKENS_API_PATH,
						URLEncoder.encode(props.getDeviceTemplate(), StandardCharsets.UTF_8.name()));
			}else {
				apiEndpoint = props.getUrlRestControlpanel() +String.format(TOKENS_API_PATH,
						URLEncoder.encode(props.getDeviceTemplate(), StandardCharsets.UTF_8.name()));
			}
			RestTemplate restTemplate;
			if (!props.isSslverify()) {
				restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
			} else {
				restTemplate = new RestTemplate();
			}
			final HttpHeaders headers = new HttpHeaders();
			headers.add(HEADER_NAME, props.getAdminAPIToken());
			final ResponseEntity<List<DeviceTokenDTO>> tokens = restTemplate.exchange(apiEndpoint, HttpMethod.GET,
					new HttpEntity<>(headers), new ParameterizedTypeReference<List<DeviceTokenDTO>>() {
			});
			initMultitenantClients(tokens.getBody());
		} else {
			Assert.notNull(props.getToken(), "No device token provided");
			client = new RestClient(props.getUrlRestIoTBroker(),
					TimeOutConfig.builder().connectTimeout(props.getConnectTimeoutInSec())
					.readTimeouts(props.getReadTimeoutInSec()).writeTimeout(props.getWriteTimeoutInSec())
					.timeunit(TimeUnit.SECONDS).build());
			client.connect(props.getToken(), props.getDeviceTemplate(), props.getDevice(), !props.isSslverify());
		}
	}

	private void initMultitenantClients(List<DeviceTokenDTO> tokens) {
		Assert.notEmpty(tokens, "No tokens found for current configured device");
		this.tokens = tokens;
		tokens.forEach(t -> {
			final RestClient restClient = new RestClient(props.getUrlRestIoTBroker(),
					TimeOutConfig.builder().connectTimeout(props.getConnectTimeoutInSec())
					.readTimeouts(props.getReadTimeoutInSec()).writeTimeout(props.getWriteTimeoutInSec())
					.timeunit(TimeUnit.SECONDS).build());
			restClient.connect(t.getToken(), props.getDeviceTemplate(), props.getDevice(), !props.isSslverify());
			clients.put(t.getTenant(), restClient);
		});
	}
}
