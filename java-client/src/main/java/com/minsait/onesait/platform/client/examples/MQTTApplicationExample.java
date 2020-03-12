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
package com.minsait.onesait.platform.client.examples;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.MQTTClient;
import com.minsait.onesait.platform.client.configuration.MQTTSecureConfiguration;
import com.minsait.onesait.platform.client.enums.LogLevel;
import com.minsait.onesait.platform.client.enums.StatusType;
import com.minsait.onesait.platform.client.exception.MqttClientException;
import com.minsait.onesait.platform.client.model.SubscriptionListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MQTTApplicationExample {

	private static final String ASTERISKS = "************************************";

	public static void main(String[] args)
			throws InterruptedException, IOException, UnrecoverableKeyException, KeyManagementException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException, MqttClientException {

		String url = "";
		String keyStorePath = "";
		String keyStorePassword = "";
		MQTTSecureConfiguration sslConfig = null;
		MQTTClient clientSecure;

		if (args == null || args.length == 0) {
			log.info(ASTERISKS);
			log.info("** onesait Platform - MQTT Example");
			log.info(ASTERISKS);
			log.info("You can  >>MQTTApplicationExample <URL_MQTT_Server>"
					+ " (where URL_MQTT_Server has this way ssl://s4citiespro.westeurope.cloudapp.azure.com:8443)");
			log.info("OR >>MQTTApplicationExample <URL_MQTT_Server> <?.jks> <password>"
					+ " (where <?.jks> can be clientdevelkeystore.jks and <password> changeIt!)");

			log.info(ASTERISKS);
			log.info("Trying >MQTTApplicationExample tcp://s4citiespro.westeurope.cloudapp.azure.com:1883");

			url = "tcp://localhost:1883";

		} else if (args.length == 3) {
			url = args[0];
			keyStorePath = args[1];
			keyStorePassword = args[2];
			sslConfig = new MQTTSecureConfiguration(keyStorePath, keyStorePassword);

		} else if (args.length == 1) {
			url = args[0];
		}

		if (sslConfig != null) {
			clientSecure = new MQTTClient(url, sslConfig);
		} else {
			clientSecure = new MQTTClient(url, false);
		}

		final int timeout = 500;
		final String token = "e7ef0742d09d4de5a3687f0cfdf7f626";
		final String deviceTemplate = "TicketingApp";
		final String device = "MQTTApp01";
		final String ontology = "Ticket";
		final String subscription = "ticketStatus";
		final String queryValue = "DONE";
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode deviceConfig = mapper.readTree(
				"[{\"action_power\":{\"shutdown\":0,\"start\":1,\"reboot\":2}},{\"action_light\":{\"on\":1,\"off\":0}}]");
		log.info("Connecting with deviceTemplate:" + deviceTemplate + " and Token:" + token + " with timeout "
				+ timeout);
		log.info("Using Ontology:" + ontology + " and instanceOntology");
		clientSecure.connect(token, deviceTemplate, device, null, "", deviceConfig);
		clientSecure.setTimeout(timeout);

		clientSecure.subscribeCommands(new SubscriptionListener() {
			@Override
			public void onMessageArrived(String message) {
				try {
					final JsonNode cmdMsg = mapper.readTree(message);
					generateLogMessage(clientSecure, timeout, cmdMsg);
				} catch (final IOException e) {

					log.error(e.getMessage());
				} catch (final MqttClientException e) {
					// TODO Auto-generated catch block
					log.error(e.getMessage());
				}

			}

		});
		final String subsId = clientSecure.subscribe(subscription, queryValue, new SubscriptionListener() {

			@Override
			public void onMessageArrived(String message) {
				try {
					final JsonNode cmdMsg = mapper.readTree(message);
					System.out.println(message);

				} catch (final IOException e) {

					log.error(e.getMessage());
				}

			}

		});
	}

	public static void generateLogMessage(MQTTClient client, int timeout, JsonNode responseMsg)
			throws MqttClientException {
		client.logCommand("Executed command " + responseMsg.get("params").toString(), 40.448277, -3.490684,
				StatusType.OK, LogLevel.INFO, responseMsg.get("commandId").asText());
	}
}
