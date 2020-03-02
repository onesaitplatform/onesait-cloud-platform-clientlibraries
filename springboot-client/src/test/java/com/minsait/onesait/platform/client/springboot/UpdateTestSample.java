package com.minsait.onesait.platform.client.springboot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerUpdate;

@IoTBrokerRepository("updateTest")
public abstract class UpdateTestSample {
	
	static final ObjectMapper mapper = new ObjectMapper();

	static final Map<String,TestData> data = new HashMap<>();
	
	//Data for the test of method1
	static final String method1 = "update1"; // the name of the method to test
	static {
		Class<?>[] params = {String.class, String.class}; //the parameters types of the method
		String ontology = "ontology";
		String query = null;
		ObjectNode instance = mapper.createObjectNode().put("instance", "instance1");
		Object[] args = { "id1", instance.toString() };
		List<JsonNode> result = null;
		TestData testData = new TestData(method1, params, ontology, query, args, result);
		data.put(method1, testData);
	}
	@IoTBrokerUpdate
    public abstract void update1(String id, String instance);
	
	//These are the methods that will be tested
	static final String[] methods = {method1};
}
