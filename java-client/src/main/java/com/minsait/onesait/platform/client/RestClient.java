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
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.MDC;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.minsait.onesait.platform.client.enums.QueryType;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class RestClient {
	@Getter
	private String sessionKey;
	@Getter
	private final String restServer;
	private final static String JOIN_GET = "rest/client/join";
	private final static String LEAVE_GET = "rest/client/leave";
	private final static String LIST_GET = "rest/ontology";
	private final static String INSERT_POST = "rest/ontology";
	private final static String UPDATE = "rest/ontology";
	private final static String DELETE = "rest/ontology";
	private final static String COMMAND = "commandAsync";

	private final static String NULL_CLIENT = "Client is null. Use connect() before.";
	private final static String UTF_8 = "UTF-8";
	private final static String QUERY_STR = "query";
	private final static String AUTHORIZATION_STR = "Authorization";
	private final static String SESSIONKEY_EXP = "Expired sessionkey detected. Regenerating";
	private final static String REGENERATING_ERROR = "Error regenerating sessionkey";
	private final static String QUERY_ERROR = "Error in query . Response:";
	private final static String APP_JSON = "application/json; charset=utf-8";
	private final static String UPDATE_ERROR = "Error in update . Response:";
	private final static String DELETE_ERROR = "Error in delete . Response:";

	public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";
	public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";

	private OkHttpClient client;
	private final ObjectMapper mapper = new ObjectMapper();
	private TimeOutConfig timeout = null;

	private String token;
	private String deviceTemplate;
	private String device;

	private final Lock lockConnection = new ReentrantLock();
	private final Lock lockRenewSession = new ReentrantLock();

	public RestClient(String restServer, TimeOutConfig timeout) {
		this.timeout = timeout;
		if (restServer.contains("iot-broker"))
			this.restServer = restServer;
		else
			this.restServer = restServer.concat("/iot-broker");
	}

	public RestClient(String restServer) {
		if (restServer.contains("iot-broker"))
			this.restServer = restServer;
		else
			this.restServer = restServer.concat("/iot-broker");
	}

	/**
	 * Creates a REST session.
	 *
	 * @param token              The token associated with the device/client
	 * @param deviceTemplate     The device/client identification
	 * @param device             The instance of the device
	 * @param avoidSSLValidation Indicates if the connection will avoid to validate
	 *                           SSL certificates
	 * @return The session key for the session established between client and IoT
	 *         Broker
	 * @throws RestException
	 *
	 */
	public String connect(String token, String deviceTemplate, String device, boolean avoidSSLValidation)
			throws SSAPConnectionException {

		try {
			lockConnection.lock();
			if (avoidSSLValidation) {
				client = getUnsafeOkHttpClient();
			} else {
				if (timeout == null)
					client = new OkHttpClient();
				else
					client = new OkHttpClient().newBuilder()
							.connectTimeout(timeout.getConnectTimeout(), timeout.getTimeunit())
							.writeTimeout(timeout.getWriteTimeout(), timeout.getTimeunit())
							.readTimeout(timeout.getReadTimeouts(), timeout.getTimeunit()).build();
			}

			return createConnection(token, deviceTemplate, device);
		} finally {
			lockConnection.unlock();
		}

	}

	public boolean isConnected() {
		try {
			lockConnection.lock();
			if (client == null) {
				log.info("client is null. Use connect(...)");
				return false;
			}
			if (sessionKey == null) {
				log.info("sessionKey is null. Use connect(...)");
				return false;
			}
			return true;
		} finally {
			lockConnection.unlock();
		}
	}

	private String createConnection(String token, String deviceTemplate, String device) throws SSAPConnectionException {
		this.token = token;
		this.deviceTemplate = deviceTemplate;
		this.device = device;

		HttpUrl url = null;
		Request request = null;
		Response response = null;
		try {
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(JOIN_GET)
					.addQueryParameter("token", token).addQueryParameter("clientPlatform", deviceTemplate)
					.addEncodedQueryParameter("clientPlatformId", device).build();
			request = new Request.Builder().url(url).addHeader(CORRELATION_ID_HEADER_NAME, logId()).get().build();
			response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				log.error("Error in createConnection . Response:" + response.toString());
				throw new SSAPConnectionException("Error in createConnection . Response:" + response.toString());
			}
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in establish connection", e);
			throw new SSAPConnectionException("Error in establish connection", e);
		}
		try {
			log.info("Connection established. Trying to join Iotbroker...");
			final JsonNode session = mapper.readTree(response.body().string());
			sessionKey = session.get("sessionKey").asText();
			log.info("Session key is :" + sessionKey);
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in get session key: {}", e);
			throw new SSAPConnectionException("Error in get Session Key", e);
		}
		return sessionKey;
	}

	// Todo --> Hay que añadir otro este método
	public List<JsonNode> query(String ontology, String query, SSAPQueryType queryType) throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		final TypeFactory typeFactory = mapper.getTypeFactory();
		try {
			final String usedSessionKey = new String(sessionKey);
			final String processedQuery = URLEncoder.encode(query, UTF_8);
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(LIST_GET)
					.addPathSegment(ontology).addEncodedQueryParameter(QUERY_STR, processedQuery)
					.addQueryParameter("queryType", SSAPQueryType.valueOf(queryType.name()).name()).build();
			request = new Request.Builder().url(url).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, usedSessionKey).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);
					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}

					return query(ontology, query, queryType);

				}

				log.error(QUERY_ERROR + response.body().string());
				throw new SSAPConnectionException(QUERY_ERROR + response.body().string());
			}
			final String instancesAsText = response.body().string();
			// instancesAsText = instancesAsText.replaceAll("\\\\\\\"", "\"").replace("\"{",
			// "{").replace("}\"", "}");
			List<JsonNode> instances = new ArrayList<>();
			instances = mapper.readValue(instancesAsText,
					typeFactory.constructCollectionType(List.class, JsonNode.class));
			return instances;
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in get instances: {}", e);
			throw new SSAPConnectionException("query: Error in get instances: {}", e);
		}

	}

	public JsonNode updateSQL(String ontology, String query) throws SSAPConnectionException {
		return updateDeleteSQL(ontology, query);
	}

	public JsonNode deleteSQL(String ontology, String query) throws SSAPConnectionException {
		return updateDeleteSQL(ontology, query);
	}

	private JsonNode updateDeleteSQL(String ontology, String query) throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		final TypeFactory typeFactory = mapper.getTypeFactory();
		try {
			final String usedSessionKey = new String(sessionKey);
			final String processedQuery = URLEncoder.encode(query, UTF_8);
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(LIST_GET)
					.addPathSegment(ontology).addEncodedQueryParameter(QUERY_STR, processedQuery)
					.addQueryParameter("queryType", SSAPQueryType.valueOf(SSAPQueryType.SQL.name()).name()).build();
			request = new Request.Builder().url(url).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, usedSessionKey).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);

					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}
					return updateDeleteSQL(ontology, query);
				}

				log.error(QUERY_ERROR + response.body().string());
				throw new SSAPConnectionException(QUERY_ERROR + response.body().string());
			}
			final String instancesAsText = response.body().string();

			return mapper.readValue(instancesAsText, JsonNode.class);
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in get instances: {}", e);
			throw new SSAPConnectionException("query: Error in get instances: {}", e);
		}

	}

	public List<JsonNode> getAll(String ontology, String query, QueryType queryType) {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		final TypeFactory typeFactory = mapper.getTypeFactory();
		try {
			List<JsonNode> instances = new ArrayList<>();
			final String usedSessionKey = new String(sessionKey);
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(LIST_GET)
					.addPathSegment(ontology).addEncodedQueryParameter(QUERY_STR, query)
					.addQueryParameter("queryType", queryType.name()).build();
			request = new Request.Builder().url(url).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, usedSessionKey).get().build();
			response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);

					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}
					return this.getAll(ontology);
				}

				log.error(QUERY_ERROR + response.body().string());
				throw new SSAPConnectionException(QUERY_ERROR + response.body().string());
			}
			String instancesAsText = response.body().string();
			instancesAsText = instancesAsText.replaceAll("\\\\\\\"", "\"").replace("\"{", "{").replace("}\"", "}");
			instances = mapper.readValue(instancesAsText,
					typeFactory.constructCollectionType(List.class, JsonNode.class));
			return instances;
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("getAll: Error in get instances: {}", e);
			throw new SSAPConnectionException("getAll: Error in get instances: {}", e);
		}
	}

	public List<JsonNode> getAll(String ontology) throws SSAPConnectionException {
		return this.getAll(ontology, "select * from " + ontology, QueryType.SQL);
	}

	// Original
	/**
	 * Si se envia un Bulk devuelve el resultado múltiple como un String
	 *
	 * @param ontology
	 * @param instance
	 * @return
	 * @throws SSAPConnectionException
	 */
	public String insert(String ontology, String instance) throws SSAPConnectionException {
		final JsonNode toReturn = sendInsert(ontology, instance);
		if (toReturn.has("id")) {
			final String idInsert = toReturn.get("id").asText();
			log.debug("Inserted ontology instance, id returned: " + idInsert);
			return idInsert;
		} else {
			final String resp = toReturn.toString();
			return resp;
		}
	}

	public JsonNode insertBulk(String ontology, String instance) throws SSAPConnectionException {
		return sendInsert(ontology, instance);
	}

	private JsonNode sendInsert(String ontology, String instance) throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;

		try {
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(INSERT_POST)
					.addPathSegment(ontology).build();
			final RequestBody body = RequestBody.create(MediaType.parse(APP_JSON), instance);

			final String usedSessionKey = new String(sessionKey);
			request = new Request.Builder().url(url).post(body).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, usedSessionKey).build();

			response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);

					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}
					return sendInsert(ontology, instance);
				}

				log.error("Error in insert . Response:" + response.body().string());
				throw new SSAPConnectionException("Error in insert . Response:" + response.body().string());
			}

			return mapper.readTree(response.body().string());

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in insert instance:", e);
			throw new SSAPConnectionException("Error in insert instance: ", e);
		}
	}

	public void update(String ontology, String instance, String id) throws SSAPConnectionException {
		this.updateWithConfirmation(ontology, instance, id, false);
	}

	public JsonNode updateWithConfirmation(String ontology, String instance, String id) throws SSAPConnectionException {
		return this.updateWithConfirmation(ontology, instance, id, true);
	}

	private JsonNode updateWithConfirmation(String ontology, String instance, String id, boolean includeIds)
			throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		try {
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(UPDATE)
					.addPathSegment(ontology).addPathSegment(id)
					.addEncodedQueryParameter("ids", Boolean.toString(includeIds)).build();

			final RequestBody body = RequestBody.create(MediaType.parse(APP_JSON), instance);

			final String usedSessionKey = new String(sessionKey);
			request = new Request.Builder().url(url).put(body).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, usedSessionKey).build();

			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);

					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}

					return updateWithConfirmation(ontology, instance, id);

				}

				log.error(UPDATE_ERROR + response.body().string());
				throw new SSAPConnectionException(UPDATE_ERROR + response.body().string());
			}

			final String responseAsText = response.body().string();
			final JsonNode instancesUpdated = mapper.readValue(responseAsText, JsonNode.class);
			return instancesUpdated;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in update", e);
			throw new SSAPConnectionException("Error in update instance:", e);
		}
	}

	public void updateQuery(String ontology, String query) throws SSAPConnectionException {
		this.updateQuery(ontology, query, false);
	}

	public JsonNode updateQueryWithIds(String ontology, String query) throws SSAPConnectionException {
		return this.updateQuery(ontology, query, true);
	}

	private JsonNode updateQuery(String ontology, String query, boolean getIds) throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		final TypeFactory typeFactory = mapper.getTypeFactory();
		try {
			final String processedQuery = URLEncoder.encode(query, UTF_8);
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(UPDATE)
					.addPathSegment(ontology).addEncodedPathSegments("update")
					.addEncodedQueryParameter(QUERY_STR, processedQuery)
					.addEncodedQueryParameter("ids", Boolean.toString(getIds)).build();
			// Es una update by query, va por GET
			final String usedSessionKey = new String(sessionKey);
			request = new Request.Builder().url(url).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, usedSessionKey).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);

					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}
					return updateQuery(ontology, query, getIds);
				}

				log.error(UPDATE_ERROR + response.body().string());
				throw new SSAPConnectionException(UPDATE_ERROR + response.body().string());
			}
			final String instancesAsText = response.body().string();
			final JsonNode instances = mapper.readValue(instancesAsText, JsonNode.class);
			return instances;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in updateByQuery", e);
			throw new SSAPConnectionException("Error in update by query:", e);
		}
	}

	/**
	 * Deletes ontology instance by Id.
	 *
	 * @param ontology Ontology associated with the message
	 * @param id       Id of the instance in DB
	 *
	 * @throws IOException
	 * @throws RestException
	 *
	 *
	 */
	public void delete(String ontology, String id) throws SSAPConnectionException {
		this.deleteWithConfirmation(ontology, id, false);
	}

	public JsonNode deleteWithConfirmation(String ontology, String id) throws SSAPConnectionException {
		return this.deleteWithConfirmation(ontology, id, true);
	}

	private JsonNode deleteWithConfirmation(String ontology, String id, boolean includeIds)
			throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		try {
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(DELETE)
					.addPathSegment(ontology).addPathSegment(id)
					.addEncodedQueryParameter("ids", Boolean.toString(includeIds)).build();

			final String usedSessionKey = new String(sessionKey);
			request = new Request.Builder().url(url).delete().addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, usedSessionKey).build();

			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);

					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}

					return deleteWithConfirmation(ontology, id);

				}

				log.error(DELETE_ERROR + response.body().string());
				throw new SSAPConnectionException(DELETE_ERROR + response.body().string());
			}

			final String responseAsText = response.body().string();
			final JsonNode instancesDeleted = mapper.readValue(responseAsText, JsonNode.class);
			return instancesDeleted;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in insert", e);
			throw new SSAPConnectionException("Error in delete instance:", e);
		}
	}

	public void deleteQuery(String ontology, String query) throws SSAPConnectionException {
		this.deleteQuery(ontology, query, false);
	}

	public JsonNode deleteQueryWithIds(String ontology, String query) throws SSAPConnectionException {
		return this.deleteQuery(ontology, query, true);
	}

	private JsonNode deleteQuery(String ontology, String query, boolean getIds) throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		final TypeFactory typeFactory = mapper.getTypeFactory();
		try {
			final String processedQuery = URLEncoder.encode(query, UTF_8);
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(DELETE)
					.addPathSegment(ontology).addEncodedPathSegments("delete")
					.addEncodedQueryParameter(QUERY_STR, processedQuery)
					.addEncodedQueryParameter("ids", Boolean.toString(getIds)).build();

			final String usedSessionKey = new String(sessionKey);
			// Es una update by query, va por GET
			request = new Request.Builder().url(url).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, usedSessionKey).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);

					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}

					return deleteQuery(ontology, query, getIds);

				}

				log.error(DELETE_ERROR + response.body().string());
				throw new SSAPConnectionException(DELETE_ERROR + response.body().string());
			}
			final String instancesAsText = response.body().string();
			final JsonNode instances = mapper.readValue(instancesAsText, JsonNode.class);
			return instances;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in update", e);
			throw new SSAPConnectionException("Error in update instance:", e);
		}
	}

	/**
	 * Sends command to connected rest device
	 *
	 * @param command The command
	 * @param data    The command parameters
	 * @throws IOException
	 */

	public void sendCommand(String command, JsonNode data) throws SSAPConnectionException {
		Response response = null;
		try {
			final HttpUrl url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(COMMAND)
					.addPathSegment(command).build();
			final RequestBody body = RequestBody.create(MediaType.parse(APP_JSON), data.textValue());

			final String usedSessionKey = new String(sessionKey);
			final Request request = new Request.Builder().url(url).post(body)
					.addHeader(CORRELATION_ID_HEADER_NAME, logId()).addHeader(AUTHORIZATION_STR, usedSessionKey)
					.build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);

					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}

					this.sendCommand(command, data);
					return;

				}
			}

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in sendCommand", e);
			throw new SSAPConnectionException("Error in send Command:", e);
		}

	}

	/**
	 * Sends command to others device
	 *
	 * @param toDeviceSession Session key of others device
	 * @param command         The command
	 * @param data            The command parameters
	 * @throws IOException
	 */
	public void sendCommand(String toDeviceSession, String command, JsonNode data) throws SSAPConnectionException {
		Response response = null;

		try {
			final HttpUrl url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(COMMAND)
					.addPathSegment(command).build();
			final RequestBody body = RequestBody.create(MediaType.parse(APP_JSON), data.textValue());

			final String usedSessionKey = new String(toDeviceSession);
			final Request request = new Request.Builder().url(url).post(body)
					.addHeader(CORRELATION_ID_HEADER_NAME, logId()).addHeader(AUTHORIZATION_STR, usedSessionKey)
					.build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);
					try {
						lockRenewSession.lock();
						if (this.sessionKey.equals(usedSessionKey)) {
							createConnection(token, deviceTemplate, device);
						}
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						lockRenewSession.unlock();
					}

					this.sendCommand(this.sessionKey, command, data);
					return;

				}
			}

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in sendCommand", e);
			throw new SSAPConnectionException("Error in send Command:", e);
		}
	}

	/**
	 * Closes REST session.
	 *
	 **/
	public void disconnect() {
		final Request request = new Request.Builder().url(restServer + "/" + LEAVE_GET)
				.addHeader(AUTHORIZATION_STR, sessionKey).addHeader(CORRELATION_ID_HEADER_NAME, logId()).get().build();
		try {
			client.newCall(request).execute();
		} catch (final IOException e) {
			log.error("Session already expired");
		}
		log.info("Disconnected");
		sessionKey = null;
	}

	private OkHttpClient getUnsafeOkHttpClient() {
		OkHttpClient.Builder builder = null;
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

			if (timeout == null)
				builder = new OkHttpClient.Builder();
			else
				builder = new OkHttpClient().newBuilder()
						.connectTimeout(timeout.getConnectTimeout(), timeout.getTimeunit())
						.writeTimeout(timeout.getWriteTimeout(), timeout.getTimeunit())
						.readTimeout(timeout.getReadTimeouts(), timeout.getTimeunit());

			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true);

			final OkHttpClient okHttpClient = builder.build();
			return okHttpClient;
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			throw new SSAPConnectionException("Error in getUnsafeOkHttpClient", e);
		}
	}

	private String logId() {
		final String logId = MDC.get(CORRELATION_ID_LOG_VAR_NAME);
		if (null == logId)
			return new String("");
		else
			return logId;
	}

}
