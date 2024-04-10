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
package com.minsait.onesait.platform.client.springboot.proxy.operations;

import java.lang.reflect.Method;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ConnectionProperties;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.extern.slf4j.Slf4j;

@Component("IoTBrokerTransaction")
@Slf4j
@EnableConfigurationProperties(ConnectionProperties.class)
public class Transaction implements Operation {

	public enum OperationType {
		UPDATE, INSERT, DELETE
	}

	@Override
	public Object operationTx(Method method, Object[] args, com.minsait.onesait.platform.client.Transaction tx,
			String ontology, Class<?> parametrizedType, boolean renewSession, OperationType operationType)
			throws SSAPConnectionException {
		Object toReturn = null;
		final ObjectMapper mapper = new ObjectMapper();
		String result = null;
		String instanceInString = null;
		String idInstance = null;
		try {

			if (operationType == OperationType.INSERT) {
				final Object ontologyArg = OperationUtil.selectOntologyArgument(args);
				instanceInString = mapper.writeValueAsString(ontologyArg);
				result = tx.insert(ontology, instanceInString);
			} else if (operationType == OperationType.UPDATE) {

				if (args[0].getClass() == String.class) {
					idInstance = (String) args[0];
					instanceInString = mapper.writeValueAsString(args[1]);
				} else {
					idInstance = (String) args[1];
					instanceInString = mapper.writeValueAsString(args[0]);
				}
				result = tx.update(ontology, instanceInString, idInstance);
			} else if (operationType == OperationType.DELETE) {

				if (args[0].getClass() == String.class) {
					idInstance = (String) args[0];
				} else {
					idInstance = (String) args[1];
				}
				result = tx.delete(ontology, idInstance);
			}
			if (method.getReturnType().getName().equals("java.lang.String")) {
				return result;
			} else if (method.getReturnType().getName().equals("void")) {
				return null;
			}
			toReturn = mapper.readValue(result, method.getReturnType());
			return toReturn;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in Transaction operation : {}", e);
			throw new SSAPConnectionException("Error in Transaction", e);
		}
	}

	@Override
	public Object operation(Method method, Object[] args, ClientIoTBroker kp, String ontology,
			Class<?> parametrizedType, boolean renewSession) throws SSAPConnectionException {
		return null;
	}

}
