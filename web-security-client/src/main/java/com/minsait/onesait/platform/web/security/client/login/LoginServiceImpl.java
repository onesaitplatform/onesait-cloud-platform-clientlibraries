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
package com.minsait.onesait.platform.web.security.client.login;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.web.security.client.domain.OAuth2AuthenticationDTO;
import com.minsait.onesait.platform.web.security.client.domain.OAuth2Token;
import com.minsait.onesait.platform.web.security.client.domain.OAuth2TokenVerification;

@Component
public class LoginServiceImpl implements LoginService {

	@Value("${openplatform.api.auth.login.path}")
	private String loginPostUrl;

	@Value("${openplatform.api.auth.token.verify.path}")
	private String loginVerifyPostUrl;

	@Value("${openplatform.api.baseurl}")
	private String baseUrl;

	@Value("${openplatform.api.auth.token.grant_type}")
	private String grantType;

	@Value("${openplatform.api.auth.token.clientId}")
	private String clientId;

	@Value("${openplatform.api.auth.token.scope}")
	private String scope;

	@Value("${openplatform.api.auth.token.vertical:onesaitplatform}")
	private String vertical;

	@Value("${openplatform.api.auth.token.password}")
	private String pwdLoginOP;

	private RestTemplate loginRestTemplate;

	@PostConstruct
	public void init() {
		loginRestTemplate = new RestTemplate();
		loginRestTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(clientId, pwdLoginOP));
	}

	@Override
	public OAuth2Token login(String username, String credentials) throws Exception {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.clear();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		final MultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
		mvm.add("grant_type", grantType);
		mvm.add("clientId", clientId);
		mvm.add("scope", scope);
		mvm.add("username", username);
		mvm.add("password", credentials);
		mvm.add("vertical", vertical);

		final HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(mvm, httpHeaders);

		ResponseEntity<OAuth2AuthenticationDTO> auth = null;

		try {
			auth = loginRestTemplate.postForEntity(baseUrl + loginPostUrl, entity, OAuth2AuthenticationDTO.class);
		} catch (final RestClientException e) {
			if (e instanceof HttpClientErrorException) {
				throw (HttpClientErrorException) e;
			} else {
				throw e;
			}

		}

		final OAuth2Token login = new OAuth2Token();
		if (auth.getBody().getAccessToken() != null && !"".equals(auth.getBody().getAccessToken())) {
			login.setToken(auth.getBody().getAccessToken());
			login.setRefreshToken(auth.getBody().getRefreshToken());
			login.setTokenType(auth.getBody().getTokenType());
			login.setExpiresIn(auth.getBody().getExpiresIn());
			if (!StringUtils.isEmpty(auth.getBody().getTenant())) {
				login.setTenant(auth.getBody().getTenant());
			}
			if (!CollectionUtils.isEmpty(auth.getBody().getVerticals())) {
				login.setVerticals(auth.getBody().getVerticals());
			}

		} else {
			throw new Exception("User information and authentication token could not be retrieved");

		}
		return login;
	}

	@Override
	public OAuth2TokenVerification verifyToken(String token) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.clear();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("token", token);
		map.add("vertical", vertical);

		final HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, httpHeaders);

		ResponseEntity<OAuth2TokenVerification> auth = null;

		try {
			auth = loginRestTemplate.postForEntity(baseUrl + loginVerifyPostUrl, entity, OAuth2TokenVerification.class);
		} catch (final RestClientException e) {
			if (e instanceof HttpClientErrorException) {
				throw (HttpClientErrorException) e;
			} else {
				throw e;
			}
		}

		return auth.getBody();
	}

}
