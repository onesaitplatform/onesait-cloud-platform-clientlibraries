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
/*******************************************************************************
 * Indra Sistemas, S.A.
 * 2013 - 2017  SPAIN
 *
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.client.springboot.proxy.operations;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.Transaction;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerInsert;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.proxy.operations.Transaction.OperationType;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.extern.slf4j.Slf4j;

@Component("IoTBrokerInsert")
@Slf4j
public class Insert implements Operation {

	@Autowired
	private OperationUtil util;

	@Override
	public Object operation(Method method, Object[] args, ClientIoTBroker client, String ontology,
			Class<?> parametrizedType, boolean renewSession) throws SSAPConnectionException {
		Object toReturn;
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final String tenantAnnotation = method.getAnnotation(IoTBrokerInsert.class).tenant();
			if (args.length < 1) {
				log.error("We need at least 1 parameter: insert(Object ontology)");
				throw new SSAPConnectionException("We need at least 1 parameter: insert(Object ontology)");
			}
			String idInsert;
			final Object ontologyArg = OperationUtil.selectOntologyArgument(args);
			final String instanceInString = mapper.writeValueAsString(ontologyArg);
			final String tenant = (String) util.parseSpEL(tenantAnnotation, args);
			if (ontologyArg instanceof List) {
				idInsert = client.init(tenant).insertBulk(ontology, instanceInString).toString();
			} else {
				idInsert = client.init(tenant).insert(ontology, instanceInString);
			}

			if (method.getReturnType().isAssignableFrom(void.class)) {

				log.warn("If you want to get id of the object stores please return String on the method");
				return null;
			}
			if (method.getReturnType().getName().equals("java.lang.String")) {
				return idInsert;
			}
			toReturn = mapper.readValue(idInsert, method.getReturnType());
			return toReturn;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in Insert operation", e);
			throw new SSAPConnectionException("Error in Insert", e);
		}
	}

	@Override
	public Object operationTx(Method method, Object[] args, Transaction tx, String ontology, Class<?> parametrizedType,
			boolean renewSession, OperationType operationType) throws SSAPConnectionException {
		return null;
	}

}
