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

	public enum RestProperty {
		URL
	}

	@Getter
	private String transactionId;

	@Getter
	private RestClientTransactional restclientTx;

	public void configureConnection(ConnectionType type, Properties prop) {
		if (type.equals(ConnectionType.REST)) {
			this.restclientTx = new RestClientTransactional(prop.getProperty(RestProperty.URL.name()));
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

	public String insert(String ontology, String instance) {
		try {
			return restclientTx.insert(ontology, instance, transactionId);
		} catch (Exception e) {
			log.error("Error inserting instance with transaction id {} . {}", transactionId, e);
			return "Error inserting instance in transaction.";
		}
	}

	public String update(String ontology, String instance, String id) {
		try {
			return restclientTx.updateById(ontology, instance, id, transactionId);
		} catch (Exception e) {
			log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			return "Error updating instance in transaction.";
		}
	}

	public String updateByQuery(String ontology, String query) {
		try {
			return restclientTx.updateByQuery(ontology, query, transactionId);
		} catch (Exception e) {
			log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			return "Error updating instance in transaction.";
		}
	}

	public String delete(String ontology, String id) {
		try {
			return restclientTx.deleteById(ontology, id, transactionId);
		} catch (Exception e) {
			log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			return "Error updating instance in transaction.";
		}
	}

	public String deleteByQuery(String ontology, String query) {
		try {
			return restclientTx.deleteByQuery(ontology, query, transactionId);
		} catch (Exception e) {
			log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			return "Error updating instance in transaction.";
		}
	}

	public String commit(Boolean lockOntologies) {
		try {
			return restclientTx.commit(transactionId, lockOntologies);
		} catch (Exception e) {
			log.error("Error commiting transaction with id {} . {}", transactionId, e);
			return "Error commiting transaction.";
		}
	}

	public String rollback() {
		try {
			return restclientTx.rollback(transactionId);
		} catch (Exception e) {
			log.error("Error rollback transaction with id {} . {}", transactionId, e);
			return "Error tollback transaction.";
		}
	}

}
