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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.client.Transaction;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestTransactionAppExample {

	public static void main(String[] args) throws SSAPConnectionException {

		final String token = "e7ef0742d09d4de5a3687f0cfdf7f626";
		final String deviceTemplate = "TicketingApp";
		final String device = "TicketMachine1";
		final String ontology = "Ticket";
		final ObjectMapper mapper = new ObjectMapper();
		Transaction tx = new Transaction("http://localhost:19000/iot-broker");
		RestClient client = null;
		try {

			log.info("Example with transaction:");
			log.info("First you can see the code of the example:");
			log.info(ExampleUtils.getInstance().loadFromResources("RestAppExample.code"));

			log.info("Now we are going to execute the example");

			client = new RestClient("http://localhost:19000/iot-broker");
			log.info("1. Connecting to {}", tx.getRestclientTx().getRestServer() + " with token:" + token
					+ " and device:" + deviceTemplate + ":" + device);
			log.info("(Rest client will accept all SSL certificates)");

			log.info("2. Starting transaction.");
			String transactionId = tx.start(token, deviceTemplate, device);
			log.info("...Started transaction. Transaction id: {}", transactionId);

			String instance = "{\"Ticket\":{\"identification\":\"WithoutTransaction\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":45.456,\"longitude\":-41.283},\"type\":\"Point\"}}}";
			log.info("3. Inserting one instance without transaction: {}", instance);
			client.connect(token, deviceTemplate, device, true);
			final String idInsert = client.insert(ontology, mapper.readTree(instance).toString());
			log.info("...Inserted instance with id {}", idInsert);

			instance = "{\"Ticket\":{\"identification\":\"WithoutTransaction\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":45.456,\"longitude\":-41.283},\"type\":\"Point\"}}}";
			log.info("4. Inserting one instance with transaction id {}: {}", transactionId, instance);
			String idInsertTx = tx.insert(ontology, mapper.readTree(instance).toString());
			log.info("...Inserted instance in transaction {} with id {}", transactionId, idInsertTx);

			instance = "{\"Ticket\":{\"identification\":\"UPDATE Transaction\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"updated\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":45.456,\"longitude\":-41.283},\"type\":\"Point\"}}}";
			log.info("5. Updating instance with transaction id {} with id {}", transactionId, idInsert);
			String idUpdateTx = tx.update(ontology, mapper.readTree(instance).toString(), idInsert);
			log.info("...Updated instance with id {}", idUpdateTx);

			instance = "{\"Ticket\":{\"identification\":\"ticket_updated\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":45.456,\"longitude\":-41.283},\"type\":\"Point\"}}}";
			log.info("6. Inserting one instance with transaction id {}: {}", transactionId, instance);
			idInsertTx = tx.insert(ontology, mapper.readTree(instance).toString());
			log.info("...Inserted instance in transaction {} with id {}", transactionId, idInsertTx);

			String query = "db.Ticket.update({'Ticket.identification':'ticket_updated'}, { $set:{'Ticket.identification':'updatedQuery'}})";
			log.info("7. Updating instance with transaction id {} with id {}", transactionId, idInsertTx);
			idUpdateTx = tx.updateByQuery(ontology, query);
			log.info("...Updated instance with id {}", idUpdateTx);

			instance = "{\"Ticket\":{\"identification\":\"ticket_delete\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":45.456,\"longitude\":-41.283},\"type\":\"Point\"}}}";
			log.info("8. Inserting one instance without transaction: {}", instance);
			idInsertTx = client.insert(ontology, mapper.readTree(instance).toString());
			log.info("...Inserted instance with id {}", idInsertTx);

			log.info("9. Deleting one instance with transaction id {}: {}", transactionId, idInsertTx);
			idInsertTx = tx.delete(ontology, idInsertTx);
			log.info("...Deleted instance in transaction {} with id {}", transactionId, idInsertTx);

			instance = "{\"Ticket\":{\"identification\":\"ticket_delete\",\"status\":\"DONE\",\"email\":\"iex@email.com\",\"name\":\"Alberto\",\"response_via\":\"email\",\"file\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"coordinates\":{\"coordinates\":{\"latitude\":45.456,\"longitude\":-41.283},\"type\":\"Point\"}}}";
			log.info("10. Inserting one instance without transaction id: {}", instance);
			idInsertTx = client.insert(ontology, mapper.readTree(instance).toString());
			log.info("...Inserted instance with id {}", idInsertTx);

			log.info("11. Deleting one instance with transaction id {}: {}", transactionId, idInsertTx);
			query = "db.Ticket.remove({'Ticket.identification':{$eq:'ticket_delete' }})";
			idInsertTx = tx.deleteByQuery(ontology, query);
			log.info("...Deleted instance in transaction {} with id {}", transactionId, idInsertTx);

			log.info("12. Commit transaction with transaction id {}", transactionId);
			tx.commit(true);
			log.info("..Transaction commited with id {}", transactionId);

			log.info("Example with transaction OK!!!");

		} catch (final Exception e) {
			log.error("Error in process", e);
			tx.rollback();
		}

	}

}
