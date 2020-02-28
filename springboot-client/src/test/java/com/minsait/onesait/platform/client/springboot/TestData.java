package com.minsait.onesait.platform.client.springboot;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

class TestData {
	final String method;
	final Class<?>[] params;
	final String ontology;
	final String query;
	final Object[] args;
	final List<JsonNode> result;
	
	TestData(String method, Class<?>[] params, String ontology, String query, Object[] args, List<JsonNode> result){
		this.method = method;
		this.params = params;
		this.ontology = ontology;
		this.query = query;
		this.args = args;
		this.result = result;
	}
}
