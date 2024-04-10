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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import javax.activation.MimetypesFileTypeMap;

import org.apache.tika.Tika;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.client.enums.RepositoryType;
import com.minsait.onesait.platform.client.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.client.model.BinaryDataFile;
import com.minsait.onesait.platform.client.utils.OkHttpClientUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
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
	private final String oauthPathTemplate = "api/login/username/{{username}}/password/{{password}}";
	private final String postAccessTokenKeycloak = "/auth/realms/{{vertical}}/protocol/openid-connect/token";
	private String oauthPath;
	private OkHttpClient client;
	private final static long MAX_SIZE = 52428800;
	private final static String AUTH = "Authorization";
	@Setter
	private String vertical = "onesaitplatform";
	private static final String DEFAULT_CLIENT = "onesaitplatform";

	public enum AuthServerType {
		OAUTH_SERVER, KEYCLOAK
	}

	public BinaryRepositoryClient(String username, String password, String server, String vertical)
			throws BinaryRepositoryException {
		this.username = username;
		this.password = password;
		this.server = server;
		if (vertical != null && !"".equals(vertical)) {
			this.vertical = vertical;
		}
		try {
			log.info("Setting Rest client to accept all SSL certificates");
			client = OkHttpClientUtil.getUnsafeOkHttpClient(null);
			compileOauthPath(AuthServerType.OAUTH_SERVER);
			authenticate(AuthServerType.OAUTH_SERVER);
			log.info("Granted access by Oauth2, token is: {}", oauthToken);
		} catch (final Exception e) {
			log.error("Error connecting with " + server + " by:" + e.getMessage());
			throw new BinaryRepositoryException("Could not get oauth credentials", e);
		}
	}

	public BinaryRepositoryClient(String username, String password, String server, AuthServerType authServerType,
			String vertical) throws BinaryRepositoryException {
		this.username = username;
		this.password = password;
		this.server = server;
		if (vertical != null && !"".equals(vertical)) {
			this.vertical = vertical;
		}

		try {
			log.info("Setting Rest client to accept all SSL certificates");
			client = OkHttpClientUtil.getUnsafeOkHttpClient(null);
			compileOauthPath(authServerType);
			authenticate(authServerType);
			log.info("Granted access by Oauth2, token is: {}", oauthToken);
		} catch (final Exception e) {
			log.error("Error connecting with " + server + " by:" + e.getMessage());
			throw new BinaryRepositoryException("Could not get oauth credentials", e);
		}
	}

	private void compileOauthPath(AuthServerType authServerType) throws UnsupportedEncodingException {
		final Writer writer = new StringWriter();
		StringReader reader = null;
		final HashMap<String, String> scopes = new HashMap<>();
		if (authServerType == AuthServerType.OAUTH_SERVER) {
			reader = new StringReader(oauthPathTemplate);
			scopes.put("username", URLEncoder.encode(username, StandardCharsets.UTF_8.name()));
			scopes.put("password", URLEncoder.encode(password, StandardCharsets.UTF_8.name()));

		} else {
			reader = new StringReader(postAccessTokenKeycloak);
			scopes.put("vertical", vertical);
		}
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(reader, "oauth path");
		mustache.execute(writer, scopes);
		oauthPath = writer.toString();

	}

	public BinaryRepositoryClient(String oauthToken, String server) {
		this.oauthToken = oauthToken;
		this.server = server;
		client = OkHttpClientUtil.getUnsafeOkHttpClient(null);
	}

	private void authenticate(AuthServerType authServerType) throws IOException {
		if (authServerType == AuthServerType.OAUTH_SERVER) {
			final HttpUrl oauth = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
					.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
					.addPathSegment(HttpUrl.parse(server).pathSegments().get(0)).addEncodedPathSegments(oauthPath)
					.addEncodedQueryParameter("vertical", vertical).build();
			final Request request = new Request.Builder().url(oauth).get().build();
			final Response response = client.newCall(request).execute();
			final JsonNode node = mapper.readTree(response.body().string());
			oauthToken = "Bearer ".concat(node.get("access_token").asText());
		} else {
			final HttpUrl oauth = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
					.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port())
					.addPathSegment(HttpUrl.parse(server).pathSegments().get(0)).addEncodedPathSegments(oauthPath)
					.build();
			final RequestBody formBody = new FormBody.Builder().addEncoded("grant_type", "password")
					.addEncoded("username", username).addEncoded("password", password).addEncoded("vertical", vertical)
					.build();
			final Request request = new Request.Builder().url(oauth).post(formBody)
					.addHeader(AUTH, "Basic "
							+ Base64.getEncoder().encodeToString((DEFAULT_CLIENT + ":" + DEFAULT_CLIENT).getBytes()))
					.build();
			final Response response = client.newCall(request).execute();
			final JsonNode node = mapper.readTree(response.body().string());
			oauthToken = "Bearer ".concat(node.get("access_token").asText());
		}
	}

	public BinaryDataFile getBinaryFile(String fileId) throws IOException, BinaryRepositoryException {
		final HttpUrl getFile = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port()).addPathSegment("controlpanel")
				.addEncodedPathSegments(BINARY_REPO_PATH).addPathSegment(fileId).build();
		final Request request = new Request.Builder().url(getFile).header(AUTH, oauthToken).get().build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			throw new BinaryRepositoryException(
					"Error while retrieving binary file, status: ".concat(String.valueOf(response.code())));
		}
		final BinaryDataFile file = mapper.readValue(response.body().string(), BinaryDataFile.class);
		log.info("Retrieved file {} with size {} Bytes", file.getFileName(), file.getData().length);
		return file;
	}

	public String addBinaryFile(File file, String metadata, RepositoryType repository)
			throws IOException, BinaryRepositoryException {
		if (file.exists() && file.length() > MAX_SIZE) {
			throw new BinaryRepositoryException("File is larger than " + MAX_SIZE + " bits");
		}
//		final MimeTypeDetector detector = new MimeTypeDetector();
		final RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("metadata", metadata).addFormDataPart("repository", repository.name())
				.addFormDataPart("file", file.getName(),
						RequestBody.create(MediaType.parse(new Tika().detect(file)), file))
				.build();

		final HttpUrl postFile = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port()).addPathSegment("controlpanel")
				.addEncodedPathSegments(BINARY_REPO_PATH).build();

		final Request request = new Request.Builder().header(AUTH, oauthToken).url(postFile).post(requestBody).build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			throw new BinaryRepositoryException(
					"Error while adding binary file, status: ".concat(String.valueOf(response.code())));
		}
		final String fileId = response.body().string();
		log.info("Added binary file to repository, id returned is {}", fileId);
		return fileId;

	}

	public void removeBinaryFile(String fileId) throws IOException, BinaryRepositoryException {
		final HttpUrl deleteFile = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port()).addPathSegment("controlpanel")
				.addEncodedPathSegments(BINARY_REPO_PATH).addPathSegment(fileId).build();
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
			throws IOException, BinaryRepositoryException {
		if (file.exists() && file.length() > MAX_SIZE) {
			throw new BinaryRepositoryException("File is larger than " + MAX_SIZE + " bits");
		}

		final RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("metadata", metadata).addFormDataPart("file", file.getName(),
						RequestBody.create(MediaType.parse(new Tika().detect(file)), file))
				.build();
		final HttpUrl postFile = new HttpUrl.Builder().scheme(HttpUrl.parse(server).scheme())
				.host(HttpUrl.parse(server).host()).port(HttpUrl.parse(server).port()).addPathSegment("controlpanel")
				.addEncodedPathSegments(BINARY_REPO_PATH).addEncodedPathSegment(fileId).build();

		final Request request = new Request.Builder().header(AUTH, oauthToken).url(postFile).put(requestBody).build();
		final Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			log.error("Error while updating binary file, response is: {}", response.body());
			throw new BinaryRepositoryException(
					"Error while adding binary file, status: ".concat(String.valueOf(response.code())));
		}

	}

	private String getFileTypeByMimetypesFileTypeMap(final String fileName) {
		final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

		return fileTypeMap.getContentType(fileName);

	}

}
