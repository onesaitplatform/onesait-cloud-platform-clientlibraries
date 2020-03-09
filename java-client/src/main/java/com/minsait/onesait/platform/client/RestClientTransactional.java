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

import java.net.URLEncoder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class RestClientTransactional extends RestClient {

	private static final String START_TX = "rest/transaction/start";
	private static final String COMMIT_TX = "rest/transaction/commit/";
	private static final String ROLLBACK_TX = "rest/transaction/rollback";
	private static final String TRANSACTION_STR = "TransactionId";
	private static final String LOCK_ONTOLOGIES_STR = "LockOntologies";
	private static final String TRANSACTION_ERROR = "Error in transaction . Response:";
	private static final String TRANSACTION_EXPIRED = "Expired sessionkey or transactionId detected. Please strat a new transaction.";

	private ObjectMapper mapper = new ObjectMapper();

	public RestClientTransactional(String restServer, TimeOutConfig timeout) {
		super(restServer, timeout);
	}

	public RestClientTransactional(String restServer) {
		super(restServer);
	}

	public String startTransaction(String token, String deviceTemplate, String device) {
		if (!isConnected()) {
			log.info("Client is not connected, generating sessionKey...");
			super.connect(token, deviceTemplate, device, true);
		}

		final JsonNode toReturn = sendStartTx();
		if (toReturn.has("transactionId")) {
			String transactionId = toReturn.get("transactionId").asText();
			log.debug("Transaction id: " + transactionId);
			return transactionId;
		} else {
			log.error("Error getting transaction id: {}", toReturn.toString());
			return null;
		}

	}

	public String insert(String ontology, String instance, String transactionId) throws SSAPConnectionException {
		final JsonNode toReturn = sendInsertTx(ontology, instance, transactionId);
		if (toReturn.has("id")) {
			final String idInsert = toReturn.get("id").asText();
			log.debug("Inserted ontology instance in transaction {}, id returned: {} ", transactionId, idInsert);
			return idInsert;
		} else {
			return toReturn.toString();
		}
	}

	public String updateById(String ontology, String instance, String id, String transactionId)
			throws SSAPConnectionException {
		final JsonNode toReturn = sendUpdateTx(ontology, instance, id, transactionId);
		if (toReturn.has("id")) {
			final String idUpdate = toReturn.get("id").asText();
			log.debug("Inserted ontology instance in transaction {}, id returned: {} ", transactionId, idUpdate);
			return idUpdate;
		} else {
			return toReturn.toString();
		}
	}

	public String updateByQuery(String ontology, String query, String transactionId) throws SSAPConnectionException {
		final JsonNode toReturn = sendUpdateQueryTx(ontology, query, transactionId);
		if (toReturn.has("id")) {
			final String idUpdate = toReturn.get("id").asText();
			log.debug("updating ontology instance in transaction {}, id returned: {} ", transactionId, idUpdate);
			return idUpdate;
		} else {
			return toReturn.toString();
		}
	}

	public String deleteById(String ontology, String id, String transactionId) throws SSAPConnectionException {
		final JsonNode toReturn = sendDeleteByIdTx(ontology, id, transactionId);
		if (toReturn.has("id")) {
			final String idDelete = toReturn.get("id").asText();
			log.debug("deleting ontology instance in transaction {}, id returned: {} ", transactionId, idDelete);
			return idDelete;
		} else {
			return toReturn.toString();
		}
	}

	public String deleteByQuery(String ontology, String query, String transactionId) throws SSAPConnectionException {
		final JsonNode toReturn = sendDeleteQueryTx(ontology, query, transactionId);
		if (toReturn.has("id")) {
			final String idDelete = toReturn.get("id").asText();
			log.debug("deleting ontology instance in transaction {}, id returned: {} ", transactionId, idDelete);
			return idDelete;
		} else {
			return toReturn.toString();
		}
	}

	public String commit(String transactionId, Boolean lockOntologies) throws SSAPConnectionException {
		final JsonNode toReturn = sendCommitTx(transactionId, lockOntologies);
		if (toReturn.has("id")) {
			final String idCommit = toReturn.get("id").asText();
			log.debug("Transaction Commit {}", transactionId);
			return idCommit;
		} else {
			return toReturn.toString();
		}
	}

	public String rollback(String transactionId) throws SSAPConnectionException {
		final JsonNode toReturn = sendRollbackTx(transactionId);
		if (toReturn.has("id")) {
			final String idrollback = toReturn.get("id").asText();
			log.debug("Transaction Rollback {}", transactionId);
			return idrollback;
		} else {
			return toReturn.toString();
		}
	}

	private JsonNode sendDeleteByIdTx(String ontology, String id, String transactionId) throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		try {
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(DELETE)
					.addPathSegment(ontology).addPathSegment(id).addEncodedQueryParameter("ids", Boolean.toString(true))
					.build();

			request = new Request.Builder().url(url).delete().addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, sessionKey).addHeader(TRANSACTION_STR, transactionId).build();

			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired transaction
					log.error(TRANSACTION_EXPIRED);
					return mapper.readTree(response.body().string());
				}

				log.error("Error deleting instance in transaction with id: {}. {}", transactionId,
						response.body().string());
				throw new SSAPConnectionException(
						"Error deleting instance in transaction with id." + response.body().string());
			}

			final String responseAsText = response.body().string();
			final JsonNode instancesDeleted = mapper.readValue(responseAsText, JsonNode.class);
			return instancesDeleted;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in delete", e);
			throw new SSAPConnectionException("Error in delete instance:", e);
		}
	}

	private JsonNode sendDeleteQueryTx(String ontology, String query, String transactionId)
			throws SSAPConnectionException {
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
					.addEncodedQueryParameter("ids", Boolean.toString(true)).build();
			// Es una update by query, va por GET
			request = new Request.Builder().url(url).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, sessionKey).addHeader(TRANSACTION_STR, transactionId).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired transaction
					log.error(TRANSACTION_EXPIRED);
					return mapper.readTree(response.body().string());
				}

				log.error("" + response.body().string());
				throw new SSAPConnectionException(
						"Error deleting instance in transaction with id." + response.body().string());
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

	private JsonNode sendStartTx() throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		final TypeFactory typeFactory = mapper.getTypeFactory();
		try {
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(START_TX)
					.build();
			request = new Request.Builder().url(url).addHeader(AUTHORIZATION_STR, sessionKey).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401 && !sessionRetry) {// Expired sessionkey
					log.info(SESSIONKEY_EXP);
					sessionRetry = true;
					try {
						super.createConnection(token, deviceTemplate, device);
						return this.sendStartTx();
					} catch (final Exception e) {
						log.error(REGENERATING_ERROR, e);
					} finally {
						sessionRetry = false;
					}
				}

				log.error(TRANSACTION_ERROR + response.body().string());
				throw new SSAPConnectionException(TRANSACTION_ERROR + response.body().string());
			}
			final String transactionIdAsText = response.body().string();
			JsonNode transactionId = mapper.readValue(transactionIdAsText, typeFactory.constructType(JsonNode.class));

			return transactionId;
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in get transaction id: {}", e);
			throw new SSAPConnectionException("start transaction: Error in get transaction id: {}", e);
		}

	}

	private JsonNode sendInsertTx(String ontology, String instance, String transactionId)
			throws SSAPConnectionException {
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

			request = new Request.Builder().url(url).post(body).addHeader(CORRELATION_ID_HEADER_NAME, super.logId())
					.addHeader(AUTHORIZATION_STR, sessionKey).addHeader(TRANSACTION_STR, transactionId).build();

			response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired TRANSACTION
					log.error(TRANSACTION_EXPIRED);
					return mapper.readTree(response.body().string());
				}

				log.error("Error in insert transaction {} . Response: {}", transactionId, response.body().string());
				throw new SSAPConnectionException(
						"Error in insert transaction " + transactionId + " . Response:" + response.body().string());
			}

			return mapper.readTree(response.body().string());

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in insert instance in transaction {}: {}", transactionId, e);
			throw new SSAPConnectionException("Error in insert with transactionId " + transactionId + " instance: ", e);
		}
	}

	private JsonNode sendUpdateTx(String ontology, String instance, String id, String transactionId)
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
					.addEncodedQueryParameter("ids", Boolean.toString(false)).build();

			final RequestBody body = RequestBody.create(MediaType.parse(APP_JSON), instance);

			request = new Request.Builder().url(url).put(body).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, sessionKey).addHeader(TRANSACTION_STR, transactionId).build();

			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired transaction
					log.error(TRANSACTION_EXPIRED);
					return mapper.readTree(response.body().string());
				}

				log.error("Error updating instance in transaction {}. Response: {}", transactionId,
						response.body().string());
				throw new SSAPConnectionException("Error updating instance in transaction " + transactionId
						+ "Response: " + response.body().string());
			}

			final String responseAsText = response.body().string();
			final JsonNode instancesUpdated = mapper.readValue(responseAsText, JsonNode.class);
			return instancesUpdated;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error updating instance in transaction {}.", transactionId);
			throw new SSAPConnectionException("Error updating instance in transaction " + transactionId + " : ", e);
		}
	}

	private JsonNode sendUpdateQueryTx(String ontology, String query, String transactionId)
			throws SSAPConnectionException {
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
					.addEncodedQueryParameter("ids", Boolean.toString(false)).build();
			// Es una update by query, va por GET
			request = new Request.Builder().url(url).addHeader(CORRELATION_ID_HEADER_NAME, logId())
					.addHeader(AUTHORIZATION_STR, sessionKey).addHeader(TRANSACTION_STR, transactionId).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired transaction
					log.error(TRANSACTION_EXPIRED);
					return mapper.readTree(response.body().string());
				}

				log.error("Error updating instance in transaction {}.", transactionId);
				throw new SSAPConnectionException("Error updating instance in transaction" + response.body().string());
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

	private JsonNode sendCommitTx(String transactionId, Boolean lockOntologies) throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		try {
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(COMMIT_TX)
					.addPathSegment(transactionId).build();
			request = new Request.Builder().url(url).addHeader(AUTHORIZATION_STR, sessionKey)
					.addHeader(LOCK_ONTOLOGIES_STR, Boolean.toString(lockOntologies)).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired transaction
					log.error(TRANSACTION_EXPIRED);
					return mapper.readTree(response.body().toString());
				}

				log.error(TRANSACTION_ERROR + response.body().string());
				throw new SSAPConnectionException(TRANSACTION_ERROR + response.body().string());
			}
			return mapper.readValue("{\"status\":\"" + response.message() + "\"}", JsonNode.class);
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error commiting transaction with id {} : {}", transactionId, e);
			throw new SSAPConnectionException("commit transaction: Error in commiting transaction.", e);
		}

	}

	private JsonNode sendRollbackTx(String transactionId) throws SSAPConnectionException {
		if (!isConnected())
			throw new SSAPConnectionException(NULL_CLIENT);
		HttpUrl url = null;
		Request request = null;
		Response response = null;
		try {
			url = new HttpUrl.Builder().scheme(HttpUrl.parse(restServer).scheme())
					.host(HttpUrl.parse(restServer).host()).port(HttpUrl.parse(restServer).port())
					.addPathSegment(HttpUrl.parse(restServer).pathSegments().get(0)).addEncodedPathSegments(ROLLBACK_TX)
					.addPathSegment(transactionId).build();
			request = new Request.Builder().url(url).addHeader(AUTHORIZATION_STR, sessionKey).get().build();
			response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				if (response.code() == 401) {// Expired transaction
					log.error(TRANSACTION_EXPIRED);
					return mapper.readTree(response.body().toString());
				}

				log.error(TRANSACTION_ERROR + response.body().string());
				throw new SSAPConnectionException(TRANSACTION_ERROR + response.body().string());
			}
			return mapper.readValue("{\"status\":\"" + response.message() + "\"}", JsonNode.class);
		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error doing rollback transaction with id {} : {}", transactionId, e);
			throw new SSAPConnectionException("rollback transaction: Error in rollback transaction.", e);
		}

	}

}
