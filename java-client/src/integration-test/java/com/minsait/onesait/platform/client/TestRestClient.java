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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;
import com.minsait.onesait.platform.testing.IntegrationTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Category(IntegrationTest.class)
public class TestRestClient {

	private final String token = "e7ef0742d09d4de5a3687f0cfdf7f626";
	private final String deviceTemplate = "TicketingApp";
	private final String device = "Rest Example App";
	private final String ontology = "Ticket";
	private final ObjectMapper mapper = new ObjectMapper();
	private static RestClient client;

	private static String url_localhost = "http://localhost:19000/iot-broker";
	private static String url_s4citiespro = "https://s4citiespro.westeurope.cloudapp.azure.com/iot-broker";

	@BeforeClass
	public static void startUp() {
		// remote
		client = new RestClient(url_localhost, TimeOutConfig.builder().connectTimeout(10).writeTimeout(10)
				.readTimeouts(10).timeunit(TimeUnit.SECONDS).build());
		// local
		// client = new RestClient("http://localhost:19000/iot-broker");
	}

	@Test
	public void test_CRUDOntologyRest() {
		try {

			log.info("Attemping to connect to {}", client.getRestServer());
			log.info("Rest client will accept all SSL certificates");
			final String sessionKey = client.connect(token, deviceTemplate, device, true);
			Assert.assertNotNull(sessionKey);

			final List<JsonNode> instancesNative = client.query(ontology, "db.Ticket.find({'Ticket.status':'PENDING'})",
					SSAPQueryType.NATIVE);
			Assert.assertNotNull(instancesNative.size() > 0);

			log.info("Inserting one instance");
			final String idInsert = client.insert(ontology, mapper.readTree(
					// "{\"Ticket\":{\"Identification\":\"\",\"Status\":\"PENDING\",\"Email\":\"iex@email.com\",\"Name\":\"Alberto\",\"Response_via\":\"Email\",\"File\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"Coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}")
					"{\"Ticket\":{\"identification\":\"111\",\"status\":\"PENDING\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}")
					.toString());
			Assert.assertNotNull(idInsert);

			log.info("Getting all existing instances");
			final List<JsonNode> instances = client.getAll(ontology);
			Assert.assertTrue(instances.size() > 0);

			log.info("Getting all existing instances filtered by query");
			final List<JsonNode> instancesFiltered = client.query(ontology,
					"select Ticket.Ticket as Ticket from Ticket where Ticket.Ticket.status=\"PENDING\"",
					SSAPQueryType.SQL);
			Assert.assertTrue(instancesFiltered.size() > 0);

			log.info("Updating instance with id {}", idInsert);
			client.update(ontology, mapper.readTree(
					"{\"Ticket\":{\"identification\":\"\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}")
					.toString(), idInsert);
			Assert.assertTrue(client.query(ontology,
					"select Ticket.Ticket as Ticket from Ticket where _id=OID(\"" + idInsert + "\")", SSAPQueryType.SQL)
					.get(0).get("Ticket").get("status").asText().equals("DONE"));
			
			log.info("Updating with native query");
			String nativeUpdate = "{\"Ticket.identification\": \"Roads bad shape\"}, {\"$set\":{\"Ticket.name\": \"Jose\"}}";
			client.updateQuery(ontology, nativeUpdate);
			List<JsonNode> response = client.query(ontology, 
			        "db.Ticket.find({\"Ticket.identification\": \"Roads bad shape\", \"Ticket.name\": \"Jose\"})", 
			        SSAPQueryType.NATIVE);
			Assert.assertTrue(response.size() > 0);
			nativeUpdate = "{\"Ticket.identification\": \"Roads bad shape\"}, {\"$set\":{\"Ticket.name\": \"Javier\"}}";
			client.updateQuery(ontology, nativeUpdate);
			response = client.query(ontology, 
                    "db.Ticket.find({\"Ticket.identification\": \"Roads bad shape\", \"Ticket.name\": \"Javier\"})", 
                    SSAPQueryType.NATIVE);
			Assert.assertTrue(response.size() > 0);
			
			log.info("Deleting instance with id {}", idInsert);
			client.delete(ontology, idInsert);
			Assert.assertTrue(client.query(ontology,
					"select Ticket.Ticket as Ticket from Ticket where _id=OID(\"" + idInsert + "\")", SSAPQueryType.SQL)
					.size() == 0);
			log.info("Disconnecting");
			client.disconnect();
			Assert.assertNull(client.getSessionKey());

		} catch (final Exception e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test(expected = SSAPConnectionException.class)
	public void when_insertingInvalidJSON_then_getsValidationError() throws Throwable {
		String sessionKey = null;
		try {
			sessionKey = client.connect(token, deviceTemplate, device, true);
			Assert.assertNotNull(sessionKey);
			log.info("Inserting one instance");
			client.insert(ontology, mapper.readTree("{\"Ticket\": 1}").toString());
		} catch (final Exception e) {
			log.error("Could not connect: {}", e);
			throw e;
		}

	}

	@Test(expected = SSAPConnectionException.class)
	public void when_providingBadCredentials_then_sessionKeyIsNotRetrieved() throws Throwable {
		client.connect("", deviceTemplate, device, true);
	}

	@Test
	public void when_insertingBulk_then_getsSizeOfInsertedOntologyInstances() throws Exception {

		String sessionKey = null;
		try {
			sessionKey = client.connect(token, deviceTemplate, device, true);
			Assert.assertNotNull(sessionKey);
			log.info("Inserting 2 ontologies");
			
			//{"nInserted":2,"inserted":["5d7b61eba7b72c4ae569ccbf","5d7b61eba7b72c4ae569ccc0"]}
			final String response = client.insert(ontology, mapper.readTree(
					"[{\"Ticket\":{\"identification\":\"111\",\"status\":\"PENDING\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"Response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}},{\"Ticket\":{\"identification\":\"111\",\"status\":\"PENDING\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":-13.887,\"longitude\":38.989},\"type\":\"Point\"}}}]")
					.toString());
			
			JsonNode responseJSON = mapper.readTree(response);
			JsonNode sizeJSON = responseJSON.get("nInserted");
			int size = sizeJSON.asInt();
			
			Assert.assertTrue(size == 2);
		} catch (final Exception e) {
			log.error("Could not connect: {}", e);
			throw e;
		}
	}
}
