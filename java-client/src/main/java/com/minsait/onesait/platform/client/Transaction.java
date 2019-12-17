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

import java.util.Properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Transaction {

	private static final String START_ERROR = "Error starting transaction. {}";

	public enum ConnectionType {
		REST, MQTT
	}

	public static final String CONNECTION_TYPE = "CONNECTION_TYPE";
	public static final String DIGITAL_BROKER_REST_ENDPOINT = "DIGITAL_BROKER_REST_ENDPOINT";

	@Getter
	private String transactionId;

	@Getter
	private RestClientTransactional restclientTx;

	public void configureConnection(Properties prop) {
		if (prop.getProperty(CONNECTION_TYPE).equals(ConnectionType.REST.name())) {
			this.restclientTx = new RestClientTransactional(prop.getProperty(DIGITAL_BROKER_REST_ENDPOINT));
		}
	}

	public String start(String token, String deviceTemplate, String device) {
		try {
			String response = restclientTx.startTransaction(token, deviceTemplate, device);
			if (response != null) {
				transactionId = response;
				return response;
			} else {
				log.error(START_ERROR);
				return null;
			}
		} catch (Exception e) {
			log.error(START_ERROR, e);
			return null;
		}
	}

	public String insert(String ontology, String instance) throws Exception {
		try {
			return restclientTx.insert(ontology, instance, transactionId);
		} catch (Exception e) {
			log.error("Error inserting instance with transaction id {} . {}", transactionId, e);
			throw new Exception("Error inserting instance with transaction id: " + transactionId);
		}
	}

	public String update(String ontology, String instance, String id) throws Exception {
		try {
			return restclientTx.updateById(ontology, instance, id, transactionId);
		} catch (Exception e) {
			log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			throw new Exception("Error inserting instance with transaction id: " + transactionId);
		}
	}

	public String updateByQuery(String ontology, String query) throws Exception {
		try {
			return restclientTx.updateByQuery(ontology, query, transactionId);
		} catch (Exception e) {
			log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			throw new Exception("Error inserting instance with transaction id: " + transactionId);
		}
	}

	public String delete(String ontology, String id) throws Exception {
		try {
			return restclientTx.deleteById(ontology, id, transactionId);
		} catch (Exception e) {
			log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			throw new Exception("Error inserting instance with transaction id: " + transactionId);
		}
	}

	public String deleteByQuery(String ontology, String query) throws Exception {
		try {
			return restclientTx.deleteByQuery(ontology, query, transactionId);
		} catch (Exception e) {
			log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			throw new Exception("Error inserting instance with transaction id: " + transactionId);
		}
	}

	public String commit(Boolean lockOntologies) {
		try {
			return restclientTx.commit(transactionId, lockOntologies);
		} catch (Exception e) {
			log.error("Error commiting transaction with id {} . {}", transactionId, e);
			return "Error inserting instance with transaction id: " + transactionId;
		}
	}

	public String rollback() {
		try {
			return restclientTx.rollback(transactionId);
		} catch (Exception e) {
			log.error("Error rollback transaction with id {} . {}", transactionId, e);
			return "Error inserting instance with transaction id: " + transactionId;
		}
	}

}
