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
package com.minsait.onesait.platform.client;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.reinert.jjschema.v1.JsonSchemaFactory;
import com.github.reinert.jjschema.v1.JsonSchemaV4Factory;
import com.minsait.onesait.platform.client.exception.NotifierException;
import com.minsait.onesait.platform.client.interfaces.Notifier;
import com.minsait.onesait.platform.client.model.Notification;
import com.minsait.onesait.platform.client.model.ValidationReport;
import com.minsait.onesait.platform.client.utils.OkHttpClientUtil;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Data
@Slf4j
public class NotifierClient implements Notifier {

	private static final String LOCALHOST = "localhost";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String ERROR_EXECUTING_REQUEST = "Error executing request:";
	private static final String APPLICATION_JSON = "application/json";
	private String server;
	private String controlpanel;
	private String identityManager;
	private String username;
	private String password;
	private String apiKey;
	private boolean avoidSSLVerification;
	private String oauthToken;
	private OkHttpClient client;
	private Lock tokenLock;

	private static final int IDENTITY_MANAGER_PORT = 21000;
	private static final int CONTROLPANEL_PORT = 18000;

	private static final String CREATE_UPDATE_PATH = "controlpanel/api/ontologies/%s/create-update";
	private static final String VALIDATE_PATH = "controlpanel/api/ontologies/%s/validate-schema";
	private static final String NOTIFIER_PATH = "controlpanel/api/notifier/notify";
	private static final String OAUTH_PATH = "oauth-server/oauth/token";
	private static final String CLIENT_ID = "onesaitplatform";
	private static final String CLIENT_SECRET = "onesaitplatform";
	private static final String SCOPE = "scope";
	private static final String SCOPE_VALUE = "openid";
	private static final String PASS_WORD = "password";
	private static final String USERNAME_FIELD = "username";
	private static final String GRANT_TYPE = "grant_type";
	private static final String VERTICAL_FIELD = "vertical";

	// TO-DO multitenant config
	private String vertical = CLIENT_ID;

	private ObjectMapper mapper = new ObjectMapper();

	public NotifierClient(String server, String username, String password, boolean avoidSSLVerification) {
		if (server == null || username == null || password == null)
			throw new IllegalArgumentException();
		this.server = server;
		this.username = username;
		this.password = password;
		this.avoidSSLVerification = avoidSSLVerification;
		init();
	}

	public NotifierClient(String server, String apiKey, boolean avoidSSLVerification) {
		if (server == null || apiKey == null)
			throw new IllegalArgumentException();
		this.server = server;
		this.apiKey = apiKey;
		this.avoidSSLVerification = avoidSSLVerification;
		init();
	}

	public void init() {
		setInternalURLs();
		setOkHttpClient();
		if (apiKey == null)
			fetchOauthToken();
		addInterceptors();
	}

	@Override
	public <T> String createOntologyFromPOJO(Class<T> clazz) {
		final JsonSchemaFactory schemaFactory = new JsonSchemaV4Factory();
		schemaFactory.setAutoPutDollarSchema(true);
		return schemaFactory.createSchema(clazz).toString();
	}

	@Override
	public <T> String createOrUpdateOntology(Class<T> clazz) {
		final String schema = createOntologyFromPOJO(clazz);
		final HttpUrl url = baseURLBuilder(controlpanel)
				.addEncodedPathSegments(String.format(CREATE_UPDATE_PATH, clazz.getSimpleName())).build();
		final RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), schema);
		final Request request = new Request.Builder().url(url).post(body).build();
		executeRequest(request, true);
		return schema;
	}

	@Override
	public String createOrUpdateOntology(String ontology, String schema) {
		final HttpUrl url = baseURLBuilder(controlpanel)
				.addEncodedPathSegments(String.format(CREATE_UPDATE_PATH, ontology)).build();
		final RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), schema);
		final Request request = new Request.Builder().url(url).post(body).build();
		executeRequest(request, true);
		return schema;
	}

	@Override
	public void validateSchema(String ontology, String input) {
		final HttpUrl url = baseURLBuilder(controlpanel).addEncodedPathSegments(String.format(VALIDATE_PATH, ontology))
				.build();
		final RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), input);
		final Request request = new Request.Builder().url(url).post(body).build();
		final Response response = executeRequest(request, true);
		try {
			if (!response.isSuccessful()) {
				if (response.body() != null) {
					log.error("Unsuccessful validation, error in response, code: , cause: ", response.code(),
							response.body().string());
				}
				throw new NotifierException("Unsuccessful validation, error in response, code: " + response.code());
			} else {
				final ValidationReport report = mapper.readValue(response.body().string(), ValidationReport.class);
				if (!report.isOk()) {
					log.error("Validation failed with following errors {}", report.getErrors());
					throw new NotifierException("Validation failed");
				}
			}
		} catch (final IOException e) {
			log.error("Could not read response body");
			throw new NotifierException("Validation failed");
		}

	}

	@Override
	public void validateSchema(Object input) {
		try {
			this.validateSchema(input.getClass().getSimpleName(), mapper.writeValueAsString(input));
		} catch (final JsonProcessingException e) {
			throw new NotifierException("Serialization exception, check input Object serialization");
		}
	}

	@Override
	public boolean notify(Notification notification) {
		final HttpUrl url = baseURLBuilder(controlpanel).addEncodedPathSegments(NOTIFIER_PATH).build();
		try {
			final RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON),
					mapper.writeValueAsString(notification));
			final Request request = new Request.Builder().url(url).post(body).build();
			final Response response = executeRequest(request, true);
			if (!response.isSuccessful()) {
				if (response.body() != null)
					log.error("Notification unsuccessful, code: {} , response: {} ", response.code(),
							response.body().string());
				else
					log.error("Notification unsuccessful, code: {}", response.code());
				return false;
			}
		} catch (final IOException e) {
			log.error("Serialization exception: ", e);
			throw new NotifierException("Serialization exception: " + e.getMessage());
		}
		return true;
	}

	@Override
	public void notifyAsync(Notification notification) {
		// TO-DO configure advanced ExecutorService
		Executors.newSingleThreadExecutor().submit(() -> notify(notification));
	}

	private void fetchOauthToken() {
		try {
			final HttpUrl url = baseURLBuilder(identityManager).addEncodedPathSegments(OAUTH_PATH).build();
			final RequestBody formBody = new FormBody.Builder()
					.addEncoded(USERNAME_FIELD, URLEncoder.encode(username, StandardCharsets.UTF_8.name()))
					.addEncoded(PASS_WORD, URLEncoder.encode(password, StandardCharsets.UTF_8.name()))
					.add(SCOPE, SCOPE_VALUE).add(GRANT_TYPE, PASS_WORD).add(VERTICAL_FIELD, vertical).build();
			final Request request = new Request.Builder().post(formBody).url(url)
					.addHeader(OkHttpClientUtil.AUTHORIZATION_STR,
							"Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
					.build();
			final Response response = client.newCall(request).execute();
			if (response.isSuccessful()) {
				final JsonNode token = mapper.readValue(response.body().string(), JsonNode.class);
				oauthToken = "Bearer " + token.get(ACCESS_TOKEN).asText();
			} else {
				throw new NotifierException("Could not fetch Oauth token: " + response.body().string());
			}
		} catch (final IOException e) {
			log.error("Error fetching Oauth token", e);
			throw new NotifierException(ERROR_EXECUTING_REQUEST, e);

		}
	}

	private void setInternalURLs() {
		if (server.toLowerCase().contains(LOCALHOST)) {
			if (server.contains(LOCALHOST + ":"))
				throw new IllegalArgumentException("Please server url must not include port, example http://localhost");
			controlpanel = server.replace(LOCALHOST, LOCALHOST + ":" + CONTROLPANEL_PORT);
			identityManager = server.replace(LOCALHOST, LOCALHOST + ":" + IDENTITY_MANAGER_PORT);
		} else {
			controlpanel = server;
			identityManager = server;
		}
	}

	private void setOkHttpClient() {
		if (avoidSSLVerification)
			client = OkHttpClientUtil.getUnsafeOkHttpClient(null);
		else
			client = OkHttpClientUtil.getSafeOkHttpClient(null);

	}

	private void addInterceptors() {
		final OkHttpClient.Builder intercepted = client.newBuilder();
		intercepted.interceptors().add(chain -> {
			final Request request = chain.request();
			Request.Builder fwdRequest;
			if (apiKey != null) {
				fwdRequest = request.newBuilder().addHeader(OkHttpClientUtil.API_KEY, apiKey);
			} else {
				fwdRequest = request.newBuilder().addHeader(OkHttpClientUtil.AUTHORIZATION_STR, oauthToken);
			}
			fwdRequest.addHeader(OkHttpClientUtil.CORRELATION_ID_HEADER_NAME, OkHttpClientUtil.logId());
			return chain.proceed(fwdRequest.build());
		});
		client = intercepted.build();

	}

	private HttpUrl.Builder baseURLBuilder(String url) {
		return new HttpUrl.Builder().scheme(HttpUrl.parse(url).scheme()).host(HttpUrl.parse(url).host())
				.port(HttpUrl.parse(url).port());
	}

	private Response executeRequest(Request request, boolean retryable) {
		final Response response;
		try {
			response = client.newCall(request).execute();
		} catch (final Exception e) {
			throw new SSAPConnectionException(ERROR_EXECUTING_REQUEST, e);
		}
		if (!response.isSuccessful()) {
			if (response.code() == 401 && retryable && apiKey == null) {
				try {
					log.debug("Invalid Oauth token, generating new one");
					tokenLock.lock();
					fetchOauthToken();
					return executeRequest(request, false);
				} catch (final Exception e) {
					log.error("Cant renew token");
				} finally {
					tokenLock.unlock();
				}

			}
			try {
				log.warn("Response failed with code {} cause: {}", response.code(), response.body().string());
			} catch (final IOException e) {
				log.warn("Response failed with code {}, no response body", response.code());
			}
		}
		return response;

	}

}
