/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.client.enums.ConfigurationType;
import com.minsait.onesait.platform.client.exception.ConfigurationManagerException;
import com.minsait.onesait.platform.client.model.Configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class ConfigurationManager {

	@Getter
	private String oauthToken;
	private final String username;
	private final String password;
	private final String server;
	private final ObjectMapper mapper = new ObjectMapper();
	private final String oauthPathTemplate = "api-ops/login/username/{{username}}/password/{{password}}";
	private String oauthPath;
	private OkHttpClient client;
	private final static String CONFIGURATION_MANAGEMENT_PATH = "management/configurations/";
	
	private final static String AUTH_STR = "Authorization";
	private final static String STATUS_STR = ", status: ";

	public ConfigurationManager(String username, String password, String server) throws ConfigurationManagerException {
		this.username = username;
		this.password = password;
		this.server = server;
		try {
			log.info("Setting Rest client to accept all SSL certificates");
			client = getUnsafeOkHttpClient();
			compileOauthPath();
			authenticate();
			log.info("Granted access by Oauth2, token is: {}", oauthToken);
		} catch (final Exception e) {
			log.error("Error connecting with " + server + " by:" + e.getMessage());
			throw new ConfigurationManagerException("Could not get oauth credentials", e);
		}

	}

	public List<Configuration> getConfigurations() throws IOException, ConfigurationManagerException {
		final HttpUrl get = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0))
				.addEncodedPathSegments(CONFIGURATION_MANAGEMENT_PATH).build();
		final Request request = new Request.Builder().url(get).header(AUTH_STR, oauthToken).get().build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new ConfigurationManagerException(
					"Error while retrieving configurations, status: ".concat(String.valueOf(response.code())));
		final List<Configuration> configurations = mapper.readValue(response.body().string(),
				new TypeReference<List<Configuration>>() {
				});
		log.info("Retrieved configurations from open platform");
		return configurations;
	}

	public Configuration getConfigurationById(String id) throws IOException, ConfigurationManagerException {
		final HttpUrl get = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0))
				.addEncodedPathSegments(CONFIGURATION_MANAGEMENT_PATH).addEncodedPathSegment(id).build();
		final Request request = new Request.Builder().url(get).header(AUTH_STR, oauthToken).get().build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new ConfigurationManagerException("Error while retrieving configuration with id " + id
					+ STATUS_STR.concat(String.valueOf(response.code())));
		final Configuration configuration = mapper.readValue(response.body().string(), Configuration.class);
		log.info("Retrieved configurations from open platform");
		return configuration;
	}

	public Configuration getConfiguration(String environment, String realm, ConfigurationType type)
			throws IOException, ConfigurationManagerException {
		final HttpUrl get = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0))
				.addEncodedPathSegments(CONFIGURATION_MANAGEMENT_PATH).addPathSegment("type")
				.addEncodedPathSegment(type.name()).addPathSegment("environment").addEncodedPathSegment(environment)
				.addPathSegment("realm").addEncodedPathSegment(realm).build();
		final Request request = new Request.Builder().url(get).header(AUTH_STR, oauthToken).get().build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new ConfigurationManagerException(
					"Error while retrieving configurations, status: ".concat(String.valueOf(response.code())));
		final Configuration configuration = mapper.readValue(response.body().string(), Configuration.class);
		log.info("Retrieved configuration of type {} from open platform", configuration.getType());
		return configuration;
	}

	public String createConfiguration(Configuration configuration) throws IOException, ConfigurationManagerException {
		final HttpUrl post = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0))
				.addEncodedPathSegments(CONFIGURATION_MANAGEMENT_PATH).build();
		final Request request = new Request.Builder().url(post).header(AUTH_STR, oauthToken).post(RequestBody
				.create(MediaType.parse("application/json; charset=utf-8"), mapper.writeValueAsString(configuration)))
				.build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new ConfigurationManagerException(
					"Error while creating configuration, status: ".concat(String.valueOf(response.code())));
		return response.body().string();

	}

	public void updateConfiguration(Configuration configuration, String id)
			throws IOException, ConfigurationManagerException {
		final HttpUrl put = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0))
				.addEncodedPathSegments(CONFIGURATION_MANAGEMENT_PATH).addEncodedPathSegment(id).build();
		final Request request = new Request.Builder().url(put).header(AUTH_STR, oauthToken).put(RequestBody
				.create(MediaType.parse("application/json; charset=utf-8"), mapper.writeValueAsString(configuration)))
				.build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new ConfigurationManagerException("Error while updating configuration with id " + id
					+ STATUS_STR.concat(String.valueOf(response.code())));

	}

	public void deleteConfiguration(String id) throws IOException, ConfigurationManagerException {
		final HttpUrl delete = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0))
				.addEncodedPathSegments(CONFIGURATION_MANAGEMENT_PATH).addEncodedPathSegment(id).build();
		final Request request = new Request.Builder().url(delete).header(AUTH_STR, oauthToken).delete().build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new ConfigurationManagerException("Error while deleting configuration with id " + id
					+ STATUS_STR.concat(String.valueOf(response.code())));

	}

	private void compileOauthPath() {
		final Writer writer = new StringWriter();
		final StringReader reader = new StringReader(oauthPathTemplate);
		final HashMap<String, String> scopes = new HashMap<String, String>();
		scopes.put("username", username);
		scopes.put("password", password);
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(reader, "oauth path");
		mustache.execute(writer, scopes);
		oauthPath = writer.toString();
	}

	private void authenticate() throws IOException {

		final HttpUrl oauth = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0)).addEncodedPathSegments(oauthPath).build();
		final Request request = new Request.Builder().url(oauth).get().build();
		final Response response = client.newCall(request).execute();
		final JsonNode node = mapper.readTree(response.body().string());
		oauthToken = "Bearer ".concat(node.get("access_token").asText());
	}

	private OkHttpClient getUnsafeOkHttpClient() {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[] {};
				}
			} };

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			final OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier(new HostnameVerifier() {

				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});

			final OkHttpClient okHttpClient = builder.build();
			return okHttpClient;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
