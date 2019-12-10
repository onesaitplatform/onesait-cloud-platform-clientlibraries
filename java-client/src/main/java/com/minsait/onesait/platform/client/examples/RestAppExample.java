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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestAppExample {

	public static void main(String[] args) throws SSAPConnectionException {

		final String token = "e7ef0742d09d4de5a3687f0cfdf7f626";
		final String deviceTemplate = "TicketingApp";
		final String device = "TicketMachine1";
		final String ontology = "Ticket";
		final ObjectMapper mapper = new ObjectMapper();
		RestClient client = null;
		try {
			log.info("Example without transaction:");
			log.info("First you can see the code of the example:");
			log.info(ExampleUtils.getInstance().loadFromResources("RestAppExample.code"));

			log.info("Now we are going to execute the example");
			client = new RestClient("http://localhost:19000/iot-broker");
			log.info("1. Connecting to {}",
					client.getRestServer() + " with token:" + token + " and device:" + deviceTemplate + ":" + device);
			log.info("(Rest client will accept all SSL certificates)");
			client.connect(token, deviceTemplate, device, true);
			log.info("...Connected to {}", client.getRestServer());

			log.info("2. Getting all existing instances of ontology:" + ontology);
			final List<JsonNode> list = client.getAll(ontology);
			log.info("...Instances returned truncated:" + list.toString().substring(0, 100));

			final String query = "select Ticket.Ticket as Ticket from Ticket where Ticket.Ticket.Status=\"PENDING\"";
			log.info("3. Getting all existing instances filtered by query:'" + query + "'");
			final List<JsonNode> instancesFiltered = client.query(ontology, query, SSAPQueryType.SQL);
			log.info("...Instances returned truncated:" + instancesFiltered.toString());

			String instance = "{\"Ticket\":{\"identification\":\"\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":45.456,\"longitude\":-41.283},\"type\":\"Point\"}}}";
			log.info("4. Inserting one instance:" + instance);
			final String idInsert = client.insert(ontology, mapper.readTree(instance).toString());
			log.info("...Inserted with id {}" + idInsert);

			instance = "{\"Ticket\":{\"identification\":\"\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"updated\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":45.456,\"longitude\":-41.283},\"type\":\"Point\"}}}";
			log.info("5. Updating instance with id {}", idInsert);
			client.update(ontology, mapper.readTree(instance).toString(), idInsert);
			log.info("...Updated instance");

			log.info("6. Deleting instance with id {}", idInsert);
			client.delete(ontology, idInsert);
			log.info("...Deleted instance");

			log.info("7. Disconnecting");
			client.disconnect();
			log.info("...Disconnected");

		} catch (final Exception e) {
			log.error("Error in process", e);
		}

	}

}
