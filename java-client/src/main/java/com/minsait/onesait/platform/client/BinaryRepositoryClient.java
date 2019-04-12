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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.overviewproject.mime_types.GetBytesException;
import org.overviewproject.mime_types.MimeTypeDetector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.client.enums.RepositoryType;
import com.minsait.onesait.platform.client.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.client.model.BinaryDataFile;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class BinaryRepositoryClient {

	@Getter
	private String oauthToken;
	private String username;
	private String password;
	private final String server;
	private final ObjectMapper mapper = new ObjectMapper();
	private final static String BINARY_REPO_PATH = "binary-repository";
	private final String oauthPathTemplate = "api-ops/login/username/{{username}}/password/{{password}}";
	private String oauthPath;
	private OkHttpClient client;
	private final static long MAX_SIZE = 52428800;
	private final static String AUTH = "Authorization";

	public BinaryRepositoryClient(String username, String password, String server) throws BinaryRepositoryException {
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
			throw new BinaryRepositoryException("Could not get oauth credentials", e);
		}
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

	public BinaryRepositoryClient(String oauthToken, String server) {
		this.oauthToken = oauthToken;
		this.server = server;
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

	public BinaryDataFile getBinaryFile(String fileId) throws IOException, BinaryRepositoryException {
		final HttpUrl getFile = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0)).addEncodedPathSegments(BINARY_REPO_PATH)
				.addPathSegment(fileId).build();
		final Request request = new Request.Builder().url(getFile).header(AUTH, oauthToken).get().build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new BinaryRepositoryException(
					"Error while retrieving binary file, status: ".concat(String.valueOf(response.code())));
		final BinaryDataFile file = mapper.readValue(response.body().string(), BinaryDataFile.class);
		log.info("Retrieved file {} with size {} Bytes", file.getFileName(), file.getData().length);
		return file;
	}

	public String addBinaryFile(File file, String metadata, RepositoryType repository)
			throws IOException, BinaryRepositoryException, GetBytesException {
		if (file.exists() && file.length() > MAX_SIZE)
			throw new BinaryRepositoryException("File is larger than " + MAX_SIZE + " bits");
		final MimeTypeDetector detector = new MimeTypeDetector();
		final RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("metadata", metadata).addFormDataPart("repository", repository.name())
				.addFormDataPart("file", file.getName(),
						RequestBody.create(MediaType.parse(detector.detectMimeType(file)), file))
				.build();
		final HttpUrl postFile = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0)).addEncodedPathSegments(BINARY_REPO_PATH)
				.build();

		final Request request = new Request.Builder().header(AUTH, oauthToken).url(postFile).post(requestBody).build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new BinaryRepositoryException(
					"Error while adding binary file, status: ".concat(String.valueOf(response.code())));
		final String fileId = response.body().string();
		log.info("Added binary file to repository, id returned is {}", fileId);
		return fileId;

	}

	public void removeBinaryFile(String fileId) throws IOException, BinaryRepositoryException {
		final HttpUrl deleteFile = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0)).addEncodedPathSegments(BINARY_REPO_PATH)
				.addPathSegment(fileId).build();
		final Request request = new Request.Builder().url(deleteFile).header(AUTH, oauthToken).delete().build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			log.error("Error while deleting binary file, response is: {}", response.body());
			throw new BinaryRepositoryException(
					"Error while deleting binary file, status: ".concat(String.valueOf(response.code())));
		}
		log.info("Delete binary file from repository with id {}", fileId);

	}

	public void updateBinaryFile(String fileId, File file, String metadata)
			throws IOException, BinaryRepositoryException, GetBytesException {
		if (file.exists() && file.length() > MAX_SIZE)
			throw new BinaryRepositoryException("File is larger than " + MAX_SIZE + " bits");
		final MimeTypeDetector detector = new MimeTypeDetector();
		final RequestBody requestBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM).addFormDataPart("metadata", metadata).addFormDataPart("file",
						file.getName(), RequestBody.create(MediaType.parse(detector.detectMimeType(file)), file))
				.build();
		final HttpUrl postFile = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
				.addPathSegment(HttpUrl.parse(server).pathSegments().get(0)).addEncodedPathSegments(BINARY_REPO_PATH)
				.addEncodedPathSegment(fileId).build();

		final Request request = new Request.Builder().header(AUTH, oauthToken).url(postFile).put(requestBody).build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			log.error("Error while updating binary file, response is: {}", response.body());
			throw new BinaryRepositoryException(
					"Error while adding binary file, status: ".concat(String.valueOf(response.code())));
		}

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
