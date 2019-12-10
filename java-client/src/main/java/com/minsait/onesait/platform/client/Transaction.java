package com.minsait.onesait.platform.client;

import jline.internal.Log;
import lombok.Getter;

public class Transaction {

	private static final String START_ERROR = "Error starting transaction. {}";

	@Getter
	private String transactionId;

	@Getter
	private RestClientTransactional restclientTx;

	public Transaction(String restClient) {
		this.restclientTx = new RestClientTransactional(restClient);
	}

	public String start(String token, String deviceTemplate, String device) {
		try {
			String response = restclientTx.startTransaction(token, deviceTemplate, device);
			if (response != null) {
				transactionId = response;
				return response;
			} else {
				Log.error(START_ERROR);
				return START_ERROR;
			}
		} catch (Exception e) {
			Log.error(START_ERROR, e);
			return START_ERROR;
		}
	}

	public String insert(String ontology, String instance) {
		try {
			return restclientTx.insert(ontology, instance, transactionId);
		} catch (Exception e) {
			Log.error("Error inserting instance with transaction id {} . {}", transactionId, e);
			return "Error inserting instance in transaction.";
		}
	}

	public String update(String ontology, String instance, String id) {
		try {
			return restclientTx.updateById(ontology, instance, id, transactionId);
		} catch (Exception e) {
			Log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			return "Error updating instance in transaction.";
		}
	}

	public String updateByQuery(String ontology, String query) {
		try {
			return restclientTx.updateByQuery(ontology, query, transactionId);
		} catch (Exception e) {
			Log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			return "Error updating instance in transaction.";
		}
	}

	public String delete(String ontology, String id) {
		try {
			return restclientTx.deleteById(ontology, id, transactionId);
		} catch (Exception e) {
			Log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			return "Error updating instance in transaction.";
		}
	}

	public String deleteByQuery(String ontology, String query) {
		try {
			return restclientTx.deleteByQuery(ontology, query, transactionId);
		} catch (Exception e) {
			Log.error("Error updating instance with transaction id {} . {}", transactionId, e);
			return "Error updating instance in transaction.";
		}
	}

	public String commit(Boolean lockOntologies) {
		try {
			return restclientTx.commit(transactionId, lockOntologies);
		} catch (Exception e) {
			Log.error("Error commiting transaction with id {} . {}", transactionId, e);
			return "Error commiting transaction.";
		}
	}

}
