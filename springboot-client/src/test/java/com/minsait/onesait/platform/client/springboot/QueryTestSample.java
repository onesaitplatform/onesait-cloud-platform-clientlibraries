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
package com.minsait.onesait.platform.client.springboot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerParam;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;

@IoTBrokerRepository("queryTest")
public abstract class QueryTestSample {
		
	static final ObjectMapper mapper = new ObjectMapper();

	static final Map<String,TestData> data = new HashMap<>();
	
	
	//Data for the test of method1
	static final String method1 = "query1"; // the name of the method to test
	static final String query1 = "select * from ontology where field1='$param1'";
	static {
		Class<?>[] params = {String.class}; //the parameters types of the method
		String ontology = "ontology";
		String query = query1;
		Object[] args = { "uno" };
		List<JsonNode> result = Arrays.asList(
				mapper.createObjectNode()
				.put("ontology", ontology)
				.put("query", "select * from ontology where field1='uno'"));
		TestData testData = new TestData(method1, params, ontology, query, args, result);
		data.put(method1, testData);
	}
	
	@IoTBrokerQuery(query1)
	public abstract List<JsonNode> query1(@IoTBrokerParam("$param1") String param1);
	
	//data for the test of method2
	static final String method2 = "query2"; //the name of the method to test
	static final String query2 = "select * from ontology where field1='$param1' and field2='$param2'";
	static {
		Class<?>[] params = {String.class, String.class}; //the parameters types of the method
		String ontology = "ontology";
		String query = query2;
		Object[] args = { "uno", "dos" };
		List<JsonNode> result = Arrays.asList(
				mapper.createObjectNode()
				.put("ontology", ontology)
				.put("query", "select * from ontology where field1='uno' and field2='dos'"));
		TestData testData = new TestData(method2, params, ontology, query, args, result);
		data.put(method2, testData);
	}
	
	@IoTBrokerQuery(query2)
	public abstract List<JsonNode> query2(@IoTBrokerParam("$param1") String param1, @IoTBrokerParam("$param2") String param2);
	
	//These are the methods that will be tested
	static final String[] methods = {method1, method2};

}
