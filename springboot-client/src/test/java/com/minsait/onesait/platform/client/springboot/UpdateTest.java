package com.minsait.onesait.platform.client.springboot;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.proxy.operations.OperationUtil;
import com.minsait.onesait.platform.client.springboot.proxy.operations.Update;

public class UpdateTest {

	@Mock
	RestClient client;
	
	@InjectMocks
	Update update;

	@Mock
	ClientIoTBroker iotBroker;

	OperationUtil util = new OperationUtil();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testUpdates() throws NoSuchMethodException, SecurityException {
		
		TestUtil.setInternalState(update, "util", util);
		
		for (String m : UpdateTestSample.methods) {
			RestClient updateClient = Mockito.mock(RestClient.class);
			ClientIoTBroker updateIotBroker = Mockito.mock(ClientIoTBroker.class);
			Mockito.when(updateIotBroker.init()).thenReturn(updateClient);
			
			Mockito.doAnswer(invocation -> {
				String ontology = invocation.getArgument(0, String.class);
				String instance = invocation.getArgument(1, String.class);
				String id = invocation.getArgument(2, String.class);
				assertEquals("Method: " + m + "-", UpdateTestSample.data.get(m).ontology, ontology);
				assertEquals("Method: " + m + "-", UpdateTestSample.data.get(m).args[0], id);
				assertEquals("Method: " + m + "-", UpdateTestSample.data.get(m).args[1], mapper.readValue(instance, String.class));				
				return null;
			}).when(updateClient).update(any(String.class), any(String.class), any(String.class));
			
			Method method = UpdateTestSample.class.getMethod(m, UpdateTestSample.data.get(m).params);
			Object result = update.operation(method, UpdateTestSample.data.get(m).args, updateIotBroker, UpdateTestSample.data.get(m).ontology, null, false);
		}
	}

}
