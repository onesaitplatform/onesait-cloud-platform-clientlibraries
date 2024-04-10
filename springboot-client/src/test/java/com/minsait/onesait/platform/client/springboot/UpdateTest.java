/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

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

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testUpdates() throws NoSuchMethodException, SecurityException {

		TestUtil.setInternalState(update, "util", util);

		for (final String m : UpdateTestSample.methods) {
			final RestClient updateClient = Mockito.mock(RestClient.class);
			final ClientIoTBroker updateIotBroker = Mockito.mock(ClientIoTBroker.class);
			Mockito.when(updateIotBroker.init(null)).thenReturn(updateClient);

			Mockito.doAnswer(invocation -> {
				final String ontology = invocation.getArgument(0, String.class);
				final String instance = invocation.getArgument(1, String.class);
				final String id = invocation.getArgument(2, String.class);
				assertEquals("Method: " + m + "-", UpdateTestSample.data.get(m).ontology, ontology);
				assertEquals("Method: " + m + "-", UpdateTestSample.data.get(m).args[0], id);
				assertEquals("Method: " + m + "-", UpdateTestSample.data.get(m).args[1],
						mapper.readValue(instance, String.class));
				return null;
			}).when(updateClient).update(any(String.class), any(String.class), any(String.class));

			final Method method = UpdateTestSample.class.getMethod(m, UpdateTestSample.data.get(m).params);
			final Object result = update.operation(method, UpdateTestSample.data.get(m).args, updateIotBroker,
					UpdateTestSample.data.get(m).ontology, null, false);
		}
	}

}
