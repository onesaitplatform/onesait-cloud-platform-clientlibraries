package com.minsait.onesait.platform.client;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.enums.LogLevel;
import com.minsait.onesait.platform.client.enums.QueryType;
import com.minsait.onesait.platform.client.enums.StatusType;
import com.minsait.onesait.platform.client.exception.MqttClientException;
import com.minsait.onesait.platform.client.model.SubscriptionListener;
import com.minsait.onesait.platform.testing.IntegrationTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Category(IntegrationTest.class)
public class TestMqtt {

	final static int timeout = 500;
	// SSL/MQTTS
	static final String url = "ssl://localhost:8883";
	static final String urlRest = "http://localhost:19000/iot-broker";
	// tcp/MQTT
	// static final String url = "tcp://localhost:1443";

	static final String token = "e7ef0742d09d4de5a3687f0cfdf7f626";
	static final String deviceTemplate = "TicketingApp";
	static final String device = "Exampletest";
	static final String ontology = "Ticket";
	static final ObjectMapper mapper = new ObjectMapper();
	static JsonNode deviceConfig;
	static final String tags = "iot, device, testing, onesait";

	static MQTTClient clientSecure;
	static RestClient restClient;

	@BeforeClass
	public static void startUp() throws JsonProcessingException, IOException {
		deviceConfig = mapper.readTree(
				"[{\"action_power\":{\"shutdown\":0,\"start\":1,\"reboot\":2}},{\"action_light\":{\"on\":1,\"off\":0}}]");
		clientSecure = new MQTTClient(url, true);
		clientSecure.setTimeout(timeout);
		restClient = new RestClient(urlRest);

	}

	@Test
	public void test_MQTT_Operations()
			throws JsonProcessingException, IOException, MqttClientException, InterruptedException {
		Thread.sleep(2000);
		log.info("Attemping to connect to broker with SSL");
		final String sessionKey = clientSecure.connect(token, deviceTemplate, device, null, tags, deviceConfig);
		Assert.assertNotNull(sessionKey);
		log.info("Session key is {}", sessionKey);

		log.info("Inserting ontology instance of type {}", ontology);
		final String idInsert = clientSecure.insert(ontology, mapper.readTree(
				"{\"Ticket\":{\"identification\":\"111\",\"status\":\"PENDING\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"Email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}")
				.toString());
		Assert.assertNotNull(idInsert);
		clientSecure.updateByQuery(ontology,
				"db.Ticket.update({'Ticket.identification':'Ticket example'},{$set:{'Ticket.identification':'Ticket example updated'}})");
		final JsonNode instanceUpdated = clientSecure.query(ontology,
				"select t.Ticket.indentification as id from Ticket as t where t.Ticket.identification=\"Ticket example updated\"",
				QueryType.SQL);
		Assert.assertNotNull(instanceUpdated);
		log.info("Subscribing to commands");
		clientSecure.subscribeCommands(new SubscriptionListener() {

			@Override
			public void onMessageArrived(String message) {
				log.info("Command received {}", message);
				Assert.assertTrue(message.contains("action_power"));
			}
		});
		restClient.connect(token, deviceTemplate, device, true);
		log.info("Sending command as a mock request from external REST action");
		restClient.sendCommand(clientSecure.getSessionKey(), "action_power",
				mapper.valueToTree("{\"action_power\":0}"));

		clientSecure.unsubscribeCommands();

		final String querySQL = "select Ticket.Ticket as Ticket from Ticket where Ticket.status=\"DONE\"";
		log.info("Subscribing to ontology {} with filtering query {}", ontology, querySQL);
		final String subscriptionId = clientSecure.subscribe(ontology, new SubscriptionListener() {

			@Override
			public void onMessageArrived(String message) {
				log.info("Message from subscription{}", message);
				Assert.assertTrue(message.contains("DONE"));

			}
		});

		Assert.assertNotNull(subscriptionId);
		clientSecure.insert(ontology, mapper.readTree(
				"{\"Ticket\":{\"identification\":\"\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"Email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}")
				.toString());

		log.info("Logging message");
		clientSecure.log("Logging a message", 56.099963, -90.09999, StatusType.OK, LogLevel.INFO);

		log.info("Unsubscribing to query {} with subsId: {}", querySQL, subscriptionId);
		clientSecure.unsubscribe(subscriptionId);

		Assert.assertTrue(!clientSecure.hasSubscription(subscriptionId));

		clientSecure.deleteById(ontology, idInsert);
		clientSecure.disconnect();
		clientSecure.close();

	}

	@Test(expected = MqttClientException.class)
	public void when_providingBadCredentials_then_sessionKeyIsNotRetrieved()
			throws InterruptedException, MqttClientException {
		Thread.sleep(2000);
		clientSecure.connect("sad", deviceTemplate, device, null, tags, deviceConfig);

	}

	@Test(expected = MqttClientException.class)
	public void when_insertingInvalidJSON_then_getsValidationError() throws Throwable {
		Thread.sleep(2000);
		String sessionKey = null;

		sessionKey = clientSecure.connect(token, deviceTemplate, device, null, tags, deviceConfig);
		Assert.assertNotNull(sessionKey);

		log.info("Inserting one instance");
		try {
			clientSecure.insert(ontology, mapper.readTree("{\"Ticket\": 1}").toString());
		} catch (final Exception e) {
			clientSecure.disconnect();
			clientSecure.close();
			throw e;
		}

	}

	@Test
	public void when_insertingBulk_then_getsSizeOfInsertedOntologyInstances() throws Exception {
		Thread.sleep(2000);
		String sessionKey = null;
		try {
			sessionKey = clientSecure.connect(token, deviceTemplate, device);
			Assert.assertNotNull(sessionKey);
			log.info("Inserting 2 ontologies");
			final String size = clientSecure.insertBulk(ontology, mapper.readTree(
					"[{\"Ticket\":{\"identification\":\"111\",\"status\":\"PENDING\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"Email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}},{\"Ticket\":{\"identification\":\"111\",\"status\":\"PENDING\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"Email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}]")
					.toString());
			Assert.assertTrue(Integer.parseInt(size) == 2);
			clientSecure.disconnect();
			clientSecure.close();
		} catch (final Exception e) {
			log.error("Could not connect: {}", e);
			throw e;
		}
	}

	@Test
	public void when_joining_andLeavingSession_andRejoiningWithSameSessionKey_thenGetsSameSessionKey()
			throws Exception {

		String sessionKey = null;
		try {
			Thread.sleep(2000);
			sessionKey = clientSecure.connect(token, deviceTemplate, device);
			Assert.assertNotNull(sessionKey);
			clientSecure.disconnect();
			Assert.assertTrue(clientSecure.connect(token, deviceTemplate, device, sessionKey).equals(sessionKey));
			clientSecure.disconnect();
			clientSecure.close();
		} catch (final Exception e) {
			log.error("Could not connect: {}", e);
			throw e;
		}
	}

	@Test
	public void whenConnecting_andDisconnecting_andInserting_thenClientAutoReconnects()
			throws InterruptedException, IOException, MqttClientException {
		Thread.sleep(2000);
		final String sessionKey = clientSecure.connect(token, deviceTemplate, device);
		Assert.assertNotNull(sessionKey);
		clientSecure.disconnect();
		Thread.sleep(1000);
		final String idInsert = clientSecure.insert(ontology, mapper.readTree(
				"{\"Ticket\":{\"identification\":\"111\",\"status\":\"PENDING\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"Email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}")
				.toString());
		Assert.assertNotNull(idInsert);
		clientSecure.deleteById(ontology, idInsert);
		clientSecure.disconnect();
		clientSecure.close();

	}

	@Test
	@Ignore
	// only to test reconnection when lost
	public void whenConnectingReconnects() throws InterruptedException, IOException, MqttClientException {
		Thread.sleep(2000);
		final String sessionKey = clientSecure.connect(token, deviceTemplate, device);
		Assert.assertNotNull(sessionKey);

		Thread.sleep(120000);
		final String idInsert = clientSecure.insert(ontology, mapper.readTree(
				"{\"Ticket\":{\"identification\":\"111\",\"status\":\"PENDING\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"Email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}")
				.toString());
		Assert.assertNotNull(idInsert);
		clientSecure.deleteById(ontology, idInsert);
		clientSecure.disconnect();
		clientSecure.close();

	}

}
