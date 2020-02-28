package com.minsait.onesait.platform.client.springboot;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.proxy.operations.OperationUtil;
import com.minsait.onesait.platform.client.springboot.proxy.operations.Query;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

public class QueryTest {

	@Mock
	RestClient client;

	@InjectMocks
	Query query;

	@Mock
	ClientIoTBroker iotBroker;

	OperationUtil util = new OperationUtil();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	private ObjectMapper mapper = new ObjectMapper();

	@Before
	public void testRestClientSetup() {
		Mockito.when(iotBroker.init()).thenReturn(client);

		Mockito.when(client.connect(any(String.class), any(String.class), any(String.class), anyBoolean()))
				.thenAnswer(invocation -> new StringBuilder()
						.append("token: ")
						.append(invocation.getArgument(0, String.class))
						.append(", deviceTemplate: ")
						.append(invocation.getArgument(1, String.class))
						.append(", deviceTemplateInstance: ")
						.append(invocation.getArgument(2, String.class))
						.toString());
		// System.out.println(client.connect("algo", "algo", "algo", true));
		
		Mockito.when(client.query(any(String.class), any(String.class), any(SSAPQueryType.class)))
				.thenAnswer(invocation -> Arrays.asList((JsonNode)mapper.createObjectNode()
						.put("ontology", invocation.getArgument(0, String.class))
						.put("query", invocation.getArgument(1, String.class)))						
				);
	}

	@Test
	public void testQueries() throws SSAPConnectionException, NoSuchMethodException, SecurityException {

		TestUtil.setInternalState(query, "util", util);
		
		for (String m : QueryTestSample.methods) {
			Method method = QueryTestSample.class.getMethod(m, QueryTestSample.data.get(m).params);
			Object result = query.operation(method, QueryTestSample.data.get(m).args, iotBroker, QueryTestSample.data.get(m).ontology, null, false);
			assertEquals("Method: " + m + "-", QueryTestSample.data.get(m).result, result);
		}
	}
}
