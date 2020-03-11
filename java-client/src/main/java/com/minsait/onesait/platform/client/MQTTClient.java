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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.client.configuration.MQTTSecureConfiguration;
import com.minsait.onesait.platform.client.enums.LogLevel;
import com.minsait.onesait.platform.client.enums.QueryType;
import com.minsait.onesait.platform.client.enums.StatusType;
import com.minsait.onesait.platform.client.exception.MqttClientException;
import com.minsait.onesait.platform.client.model.SubscriptionListener;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.json.SSAPJsonParser;
import com.minsait.onesait.platform.comms.protocol.json.Exception.SSAPParseException;
import com.minsait.onesait.platform.comms.protocol.util.SSAPMessageGenerator;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MQTTClient {

	private final MemoryPersistence persistence = new MemoryPersistence();

	private final String brokerURL;
	private final String topic = "/message";
	private final String topic_message = "/topic/message";
	private final String topic_subscription = "/topic/subscription";
	private final String topic_subscription_command = "/topic/command";

	private final String BROKER_MESSAGE_ERROR = "Could not retrieve message from broker, interrupted thread";
	private final String NOT_RESULT = "Could not get result from retrieved message at CompletableFuture object";
	private final String SSAP_MESSAGE_ERROR = "Could not parse SSAP message";
	private final String LOST_CONNECTION = "Connection to broker lost: ";
	private final String TIMEOUT_STR = "Timeout";
	private final String INSERT_MESSAGE_ERROR = "Could not publish insert message \nError Code: ";

	private MqttClient client;
	private final boolean useSSL;
	@Getter
	public String sessionKey;
	@Getter
	public boolean subscribedToCommands;
	@Setter
	public int timeout = 5;
	private MQTTSecureConfiguration sslConfig;
	private final Map<String, SubscriptionListener> subscriptions = new HashMap<String, SubscriptionListener>();
	private CompletableFuture<String> completableFutureMessage = new CompletableFuture<String>();

	@Getter
	private String token;
	@Getter
	private String device;
	@Getter
	private String deviceTemplate;

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Initializes MQTTClient
	 *
	 * @param brokerURL Broker URL.
	 * @param useSSL    If set to true, default MQTTS configuration will be used.
	 */
	public MQTTClient(String brokerURL, boolean useSSL) {
		this.brokerURL = brokerURL;
		this.useSSL = useSSL;
	}

	/**
	 * Initializes MQTTClient
	 *
	 * @param brokerURL Broker URL.
	 * @param sslConfig SSL (MQTTS) Configuration to be used.
	 */
	public MQTTClient(String brokerURL, MQTTSecureConfiguration sslConfig) {
		this.brokerURL = brokerURL;
		useSSL = true;
		this.sslConfig = sslConfig;

	}

	/**
	 * Creates a MQTT session.
	 *
	 * @param token          The token associated with the device template generated
	 *                       by the platform
	 * @param deviceTemplate The device template identification in the platform
	 * @param device         The identification of the device that will be connected
	 *                       to the platform
	 * @param tags           Tags for this device, separated by commas.
	 * @return The session key for the session established between client and IoT
	 *         Broker
	 * @throws MqttClientException
	 */
	public String connect(String token, String deviceTemplate, String device) throws MqttClientException {
		return connect(token, deviceTemplate, device, null, null, null);
	}

	public String connect(String token, String deviceTemplate, String device, String sessionKey)
			throws MqttClientException {
		return connect(token, deviceTemplate, device, sessionKey, null, null);
	}

	/**
	 * Creates a MQTT session.
	 *
	 * @param token                The token associated with the device template
	 *                             generated by the platform
	 * @param deviceTemplate       The device template identification in the
	 *                             platform
	 * @param device               The identification of the device that will be
	 *                             connected to the platform
	 * @param tags                 Tags for this device, separated by commas.
	 * @param commandConfiguration A JSON object containing command configuration
	 *                             for this device
	 * @return The session key for the session established between client and IoT
	 *         Broker
	 * @throws MqttClientException
	 */
	@SuppressWarnings("unchecked")
	public String connect(String token, String deviceTemplate, String device, String sessionKeyOld, String tags,
			JsonNode commandConfiguration) throws MqttClientException {
		try {
			this.token = token;
			this.deviceTemplate = deviceTemplate;
			this.device = device;
			// Connect client MQTT
			client = new MqttClient(brokerURL, device, persistence);
			client.setTimeToWait(5000);
			// Unsecure connection
			if (!useSSL) {
				log.debug("Using MQTT protocol. Unsecure");
				client.connect();
			} else if (useSSL && sslConfig != null) {
				log.debug("Using MQTTS with custom configuration");
				final MqttConnectOptions options = new MqttConnectOptions();
				options.setSocketFactory(sslConfig.configureSSLSocketFactory());
				client.connect(options);
			} else if (useSSL && sslConfig == null) {
				log.debug("Using MQTTS with default configuration");
				final MqttConnectOptions options = new MqttConnectOptions();
				sslConfig = new MQTTSecureConfiguration(null, null);
				options.setSocketFactory(sslConfig.configureSSLSocketFactory());
				client.connect(options);
			} else
				throw new MqttClientException("Could no connect MQTT client, bad SSL initialization");

			log.debug("Subscribing to message topic");
			client.subscribe(topic_message + "/" + client.getClientId(), new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					final String response = new String(message.getPayload());
					completableFutureMessage.complete(response);
					completableFutureMessage = new CompletableFuture<String>();
				}
			});

			// SEND JOIN
			final MqttMessage mqttJoin = new MqttMessage();
			mqttJoin.setPayload(
					SSAPJsonParser.getInstance().serialize(SSAPMessageGenerator.generateRequestJoinMessage(token,
							deviceTemplate, device, sessionKeyOld, tags, commandConfiguration)).getBytes());
			client.publish(topic, mqttJoin);

			// GET JOIN RESPONSE
			final String joinResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);
			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance().deserialize(joinResponse);
			if (response.getSessionKey() != null) {
				sessionKey = response.getSessionKey();
			} else
				throw new MqttClientException("Could not stablish connection, error code is "
						+ response.getBody().getErrorCode() + ":" + response.getBody().getError());

		} catch (final MqttException e) {
			throw new MqttClientException("Could not connect to Broker", e);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException("Timeout, could not retrieve session key", e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		} catch (final Exception e) {
			throw new MqttClientException("Error", e);
		}

		return sessionKey;
	}

	/**
	 * Subscribe to commands.
	 *
	 * @param listener Callback listener that will handle command messages.
	 *                 Resulting message will have JSON structure, with parameters:
	 *                 Command, CommandId, Params
	 * @throws MqttClientException
	 *
	 */
	public void subscribeCommands(SubscriptionListener listener) throws MqttClientException {
		try {
			client.subscribe(topic_subscription_command + "/" + sessionKey, new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					final String response = new String(message.getPayload());
					delegateCommandMessage(response, listener);

				}
			});
		} catch (final MqttException e) {
			throw new MqttClientException("Could not subscribe to commands", e);
		}
	}

	/**
	 * Unsubscribe to commands.
	 *
	 * @throws MqttClientException
	 *
	 */

	public void unsubscribeCommands() throws MqttClientException {
		try {
			client.unsubscribe(topic_subscription_command + "/" + sessionKey);

		} catch (final MqttException e) {
			log.error("Could not unsubscribe to commands: {}", e);
			throw new MqttClientException("Could not unsubscribe to commands", e);
		}
	}

	/**
	 * Subscribes this device to specific o ontology and query.
	 *
	 * @param ontology Ontology to be subscribed to
	 * @param listener Callback listener that will handle subscription messages.
	 *                 Resulting message will have JSON structure, with parameters:
	 *                 ontology and data.
	 * @return The subscription ID
	 * @throws MqttClientException
	 */

	@SuppressWarnings("unchecked")
	public String subscribe(String subscription, String queryValue, SubscriptionListener listener)
			throws MqttClientException {
		String subscriptionId = null;

		try {

			final MqttMessage message = new MqttMessage();
			message.setPayload(SSAPJsonParser.getInstance().serialize(SSAPMessageGenerator
					.generateRequestSubscriptionMessage(subscription, queryValue, null, sessionKey, device))
					.getBytes());
			client.publish(topic, message);

			// GET SUBS RESPONSE ACK
			final String subsResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);

			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance().deserialize(subsResponse);
			if (response.getBody().isOk()) {
				subscriptionId = response.getBody().getData().get("subscriptionId").asText();
				log.debug("Subscribed to subscription " + subscription);

				if (!subscriptions.containsKey(subscriptionId))
					subscriptions.put(subscriptionId, listener);

				client.subscribe(topic_subscription + "/" + sessionKey, new IMqttMessageListener() {
					@Override
					public void messageArrived(String topic, MqttMessage message) throws Exception {

						final String response = new String(message.getPayload());
						delegateMessageFromSubscription(response);

					}
				});
			} else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					return subscribe(subscription, queryValue, listener);

				} else {
					log.error("Error subscribing client to topic. {}", response.getBody().getError());
				}

			}

		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			return subscribe(subscription, queryValue, listener);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException("Timeout ", e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}

		return subscriptionId;
	}

	/**
	 * Unsubscribe to Ontology
	 *
	 * @param subscriptionId The subscription ID
	 * @throws MqttClientException
	 *
	 */

	@SuppressWarnings("unchecked")
	public void unsubscribe(String subscriptionId) throws MqttClientException {

		try {

			final MqttMessage message = new MqttMessage();
			message.setPayload(SSAPJsonParser.getInstance()
					.serialize(SSAPMessageGenerator.generateRequestUnsubscribeMessage(sessionKey, subscriptionId))
					.getBytes());
			client.publish(topic, message);

			// GET SUBS RESPONSE
			final String subsResponse = completableFutureMessage.get();
			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance().deserialize(subsResponse);
			if (response.getBody().isOk()) {
				subscriptions.remove(subscriptionId);
				client.unsubscribe(topic_subscription + "/" + sessionKey);
			} else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					unsubscribe(subscriptionId);
				}

				throw new MqttClientException("Could not unsubscribe to subscription");
			}

		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			unsubscribe(subscriptionId);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}
	}

	/**
	 * Publishes a log message to Broker, in response of a received command.
	 *
	 * @param message   Log message
	 * @param latitude  Latitude (geolocation)
	 * @param longitude Longitude (geolocation)
	 * @param status    Status of the device
	 * @param level     Log level of this message
	 * @param commandId Identification of the command associated to this log message
	 * @throws MqttClientException
	 *
	 */

	@SuppressWarnings("unchecked")
	public void logCommand(String message, double latitude, double longitude, StatusType status, LogLevel level,
			String commandId) throws MqttClientException {

		try {
			final MqttMessage mqttLog = new MqttMessage();
			mqttLog.setPayload(
					SSAPJsonParser.getInstance().serialize(SSAPMessageGenerator.generateLogMessage(sessionKey, latitude,
							longitude, level.name(), status.name(), commandId, message)).getBytes());

			client.publish(topic, mqttLog);
			final String response = completableFutureMessage.get(timeout, TimeUnit.SECONDS);
			final SSAPMessage<SSAPBodyReturnMessage> responseSSAP = SSAPJsonParser.getInstance().deserialize(response);
			if (responseSSAP.getBody().isOk())
				log.debug("Log message published");
			else {
				if (isSessionExpired(responseSSAP)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					logCommand(message, latitude, longitude, status, level, commandId);
				}

				throw new MqttClientException("Could not publish message \nError Code: "
						+ responseSSAP.getBody().getErrorCode() + ":\n" + responseSSAP.getBody().getError());
			}
		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			logCommand(message, latitude, longitude, status, level, commandId);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException(TIMEOUT_STR, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}
	}

	/**
	 * Publishes a message through MQTT session.
	 *
	 * @param message   Log message
	 * @param latitude  Latitude (geolocation)
	 * @param longitude Longitude (geolocation)
	 * @param status    Status of the device
	 * @param level     Log level
	 * @throws MqttClientException
	 *
	 *
	 */

	@SuppressWarnings("unchecked")
	public void log(String message, double latitude, double longitude, StatusType status, LogLevel level)
			throws MqttClientException {

		try {
			final MqttMessage mqttLog = new MqttMessage();
			mqttLog.setPayload(
					SSAPJsonParser.getInstance().serialize(SSAPMessageGenerator.generateLogMessage(sessionKey, latitude,
							longitude, level.name(), status.name(), null, message)).getBytes());
			client.publish(topic, mqttLog);
			final String response = completableFutureMessage.get(timeout, TimeUnit.SECONDS);
			final SSAPMessage<SSAPBodyReturnMessage> responseSSAP = SSAPJsonParser.getInstance().deserialize(response);
			if (responseSSAP.getBody().isOk())
				log.debug("Log message published");
			else {
				if (isSessionExpired(responseSSAP)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					log(message, latitude, longitude, status, level);
				}

				throw new MqttClientException("Could not publish message \nError Code: "
						+ responseSSAP.getBody().getErrorCode() + ":\n" + responseSSAP.getBody().getError());
			}
		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			log(message, latitude, longitude, status, level);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException(TIMEOUT_STR, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}
	}

	/**
	 * Inserts data to an ontology
	 *
	 * @param ontology The ontology
	 * @param jsonData The instance
	 * @throws MqttClientException
	 */
	@SuppressWarnings("unchecked")
	public String insert(String ontology, String jsonData) throws MqttClientException {

		try {

			final MqttMessage mqttInsert = new MqttMessage();
			mqttInsert.setPayload(SSAPJsonParser.getInstance().serialize(
					SSAPMessageGenerator.generateRequestInsertMessage(sessionKey, ontology, mapper.readTree(jsonData)))
					.getBytes());

			client.publish(topic, mqttInsert);

			final String insertResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);

			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance()
					.deserialize(insertResponse);
			if (response.getBody().isOk())
				log.debug("Insert message published");
			else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					return insert(ontology, jsonData);
				}
				throw new MqttClientException(INSERT_MESSAGE_ERROR + response.getBody().getErrorCode() + ":\n"
						+ response.getBody().getError());
			}
			return response.getBody().getData().get("id").asText();
		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			return insert(ontology, jsonData);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException(TIMEOUT_STR, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		} catch (final IOException e) {
			throw new MqttClientException("Could not read ontology instance", e);
		}
	}

	/**
	 * Inserts bulk data to an ontology
	 *
	 * @param ontology The ontology
	 * @param jsonData The array of instances
	 * @throws MqttClientException
	 */
	@SuppressWarnings("unchecked")
	public String insertBulk(String ontology, String jsonData) throws MqttClientException {

		try {

			final MqttMessage mqttInsert = new MqttMessage();
			mqttInsert.setPayload(SSAPJsonParser.getInstance().serialize(
					SSAPMessageGenerator.generateRequestInsertMessage(sessionKey, ontology, mapper.readTree(jsonData)))
					.getBytes());

			client.publish(topic, mqttInsert);

			final String insertResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);

			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance()
					.deserialize(insertResponse);
			if (response.getBody().isOk())
				log.debug("Insert message published");
			else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					return insert(ontology, jsonData);
				}
				throw new MqttClientException("Could not publish insert message \nError Code: "
						+ response.getBody().getErrorCode() + ":\n" + response.getBody().getError());
			}
			return response.getBody().getData().get("nInserted").asText();
		} catch (final MqttException e) {
			handleMqttException("Connection to broker lost: " + e.getMessage());
			return insert(ontology, jsonData);
		} catch (final InterruptedException e) {
			throw new MqttClientException("Could not retrieve message from broker, interrupted thread", e);
		} catch (final ExecutionException e) {
			throw new MqttClientException("Could not get result from retrieved message at CompletableFuture object", e);
		} catch (final TimeoutException e) {
			throw new MqttClientException("Timeout", e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException("Could not parse SSAP message", e);
		} catch (final IOException e) {
			throw new MqttClientException("Could not read ontology instance", e);
		}
	}

	/****
	 *
	 * @param ontology  The ontology whose data is going to be update
	 * @param query     The update query (native only)
	 * @param queryType Type of the query, native or sql
	 * @throws MqttClientException
	 */

	@SuppressWarnings("unchecked")
	public JsonNode query(String ontology, String query, QueryType queryType) throws MqttClientException {

		try {

			final MqttMessage mqttQuery = new MqttMessage();
			mqttQuery.setPayload(
					SSAPJsonParser.getInstance().serialize(SSAPMessageGenerator.generateQueryMessage(sessionKey,
							ontology, query, SSAPQueryType.valueOf(queryType.name()))).getBytes());

			client.publish(topic, mqttQuery);

			final String queryResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);

			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance().deserialize(queryResponse);
			if (response.getBody().isOk())
				log.debug("Query operation successful");
			else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					return query(ontology, query, queryType);
				}
				throw new MqttClientException(INSERT_MESSAGE_ERROR + response.getBody().getErrorCode() + ":\n"
						+ response.getBody().getError());
			}
			return response.getBody().getData();
		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			return query(ontology, query, queryType);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException(TIMEOUT_STR, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}
	}

	/****
	 *
	 * @param ontology The ontology whose data is going to be update
	 * @param id       The id of the instance
	 * @param data     The new instance of the ontology
	 * @throws MqttClientException
	 */

	@SuppressWarnings("unchecked")
	public void updateById(String ontology, String id, JsonNode data) throws MqttClientException {

		try {

			final MqttMessage mqttUpdate = new MqttMessage();
			mqttUpdate.setPayload(SSAPJsonParser.getInstance()
					.serialize(SSAPMessageGenerator.generateUpdateByIdMessage(sessionKey, ontology, id, data))
					.getBytes());

			client.publish(topic, mqttUpdate);

			final String updateResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);

			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance()
					.deserialize(updateResponse);
			if (response.getBody().isOk())
				log.debug("Update operation successful");
			else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					updateById(ontology, id, data);
				}
				throw new MqttClientException(INSERT_MESSAGE_ERROR + response.getBody().getErrorCode() + ":\n"
						+ response.getBody().getError());
			}
		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			updateById(ontology, id, data);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException(TIMEOUT_STR, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}

	}

	/****
	 *
	 * @param ontology The ontology whose data is going to be update
	 * @param query    The update query (native only)
	 * @throws MqttClientException
	 */

	@SuppressWarnings("unchecked")
	public void updateByQuery(String ontology, String query) throws MqttClientException {

		try {

			final MqttMessage mqttUpdate = new MqttMessage();
			mqttUpdate.setPayload(SSAPJsonParser.getInstance()
					.serialize(SSAPMessageGenerator.generateUpdateByNativeQueryMessage(sessionKey, ontology, query))
					.getBytes());

			client.publish(topic, mqttUpdate);

			final String updateResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);

			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance()
					.deserialize(updateResponse);
			if (response.getBody().isOk())
				log.debug("Update operation successful");
			else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					updateByQuery(ontology, query);
				}
				throw new MqttClientException(INSERT_MESSAGE_ERROR + response.getBody().getErrorCode() + ":\n"
						+ response.getBody().getError());
			}
		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			updateByQuery(ontology, query);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException(TIMEOUT_STR, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}

	}

	/****
	 *
	 * @param ontology The ontology whose data is going to be deleted
	 * @param query    The filter for the delete operation (native only)
	 * @throws MqttClientException
	 */

	@SuppressWarnings("unchecked")
	public void deleteByQuery(String ontology, String query) throws MqttClientException {

		try {

			final MqttMessage mqttDelete = new MqttMessage();
			mqttDelete.setPayload(SSAPJsonParser.getInstance()
					.serialize(SSAPMessageGenerator.generateDeteteByNativeQueryMessage(sessionKey, ontology, query))
					.getBytes());

			client.publish(topic, mqttDelete);

			final String deleteResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);

			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance()
					.deserialize(deleteResponse);
			if (response.getBody().isOk())
				log.debug("Delete operation successful");
			else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					deleteByQuery(ontology, query);
				}
				throw new MqttClientException(INSERT_MESSAGE_ERROR + response.getBody().getErrorCode() + ":\n"
						+ response.getBody().getError());
			}
		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			deleteByQuery(ontology, query);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException(TIMEOUT_STR, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}

	}

	/***
	 *
	 * @param ontology The ontology whose data is going to be deleted
	 * @param id       The id of the instance
	 * @throws MqttClientException
	 */
	@SuppressWarnings("unchecked")
	public void deleteById(String ontology, String id) throws MqttClientException {

		try {

			final MqttMessage mqttDelete = new MqttMessage();
			mqttDelete.setPayload(SSAPJsonParser.getInstance()
					.serialize(SSAPMessageGenerator.generateDeteteByIdMessage(sessionKey, ontology, id)).getBytes());

			client.publish(topic, mqttDelete);

			final String deleteResponse = completableFutureMessage.get(timeout, TimeUnit.SECONDS);

			final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance()
					.deserialize(deleteResponse);
			if (response.getBody().isOk())
				log.debug("Delete operation successful");
			else {
				if (isSessionExpired(response)) {
					this.connect(token, deviceTemplate, device, sessionKey);
					deleteById(ontology, id);
				}
				throw new MqttClientException(INSERT_MESSAGE_ERROR + response.getBody().getErrorCode() + ":\n"
						+ response.getBody().getError());
			}
		} catch (final MqttException e) {
			handleMqttException(LOST_CONNECTION + e.getMessage());
			deleteById(ontology, id);
		} catch (final InterruptedException e) {
			throw new MqttClientException(BROKER_MESSAGE_ERROR, e);
		} catch (final ExecutionException e) {
			throw new MqttClientException(NOT_RESULT, e);
		} catch (final TimeoutException e) {
			throw new MqttClientException(TIMEOUT_STR, e);
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);
		}

	}

	/**
	 * Closes MQTT session.
	 *
	 * @throws MqttClientException
	 *
	 **/
	public void disconnect() throws MqttClientException {

		try {
			// MQTT LEAVE MESSAGE
			final MqttMessage mqttLeave = new MqttMessage();
			mqttLeave.setPayload(SSAPJsonParser.getInstance()
					.serialize(SSAPMessageGenerator.generateRequestLeaveMessage(sessionKey)).getBytes());
			client.publish(topic, mqttLeave);
			client.disconnect();
			log.debug("Disconnecting from the server");
		} catch (final SSAPParseException e) {
			throw new MqttClientException(SSAP_MESSAGE_ERROR, e);

		} catch (final MqttException e) {
			handleMqttException("Could not disconnect successfully from broker: " + e.getMessage());
		}
		log.debug("Session clossed");

	}

	private void delegateMessageFromSubscription(String message) throws JsonProcessingException, IOException {

		final JsonNode indMsg = mapper.createObjectNode();
		((ObjectNode) indMsg).set("data", mapper.readTree(message).get("body").get("data"));
		final String subsId = mapper.readTree(message).get("body").get("subscriptionId").asText();
		((ObjectNode) indMsg).put("subscriptionId", subsId);
		final SubscriptionListener listener = subscriptions.get(subsId);
		new Thread(new Runnable() {
			@Override
			public void run() {
				listener.onMessageArrived(indMsg.toString());
			}
		}).start();

	}

	private void delegateCommandMessage(String message, SubscriptionListener listener) throws IOException {
		final JsonNode cmdMsg = mapper.createObjectNode();
		final String command = mapper.readTree(message).get("body").get("command").asText();
		final JsonNode params = mapper.readTree(message).get("body").get("params");
		final String commandId = mapper.readTree(message).get("body").get("commandId").asText();
		((ObjectNode) cmdMsg).put("commandId", commandId);
		((ObjectNode) cmdMsg).put("command", command);
		((ObjectNode) cmdMsg).set("params", params);
		new Thread(new Runnable() {
			@Override
			public void run() {
				listener.onMessageArrived(cmdMsg.toString());
			}
		}).start();

	}

	public boolean hasSubscription(String subscriptionId) {
		return subscriptions.containsKey(subscriptionId);
	}

	private boolean isSessionExpired(SSAPMessage<SSAPBodyReturnMessage> message) {
		if (message.getDirection().equals(SSAPMessageDirection.ERROR)
				&& message.getBody().getErrorCode().equals(SSAPErrorCode.AUTENTICATION)) {
			log.debug("Sessionkey expired, attemping to reconnect");
			return true;
		}

		else
			return false;
	}

	private void handleMqttException(String message) throws MqttClientException {
		log.error(message);
		if (sessionKey != null) {
			log.debug("Client is not connected, attemping to reconnect");
			this.connect(token, deviceTemplate, device, sessionKey);
		} else
			throw new MqttClientException(message);
	}

	public void close() {
		try {
			client.disconnect();
		} catch (final MqttException e) {
			log.info("Already disconnected, clossing client...");
		}
		client = null;
		sessionKey = null;
		device = null;
		deviceTemplate = null;
		token = null;
	}
}
