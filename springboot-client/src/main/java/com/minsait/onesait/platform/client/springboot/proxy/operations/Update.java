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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.Transaction;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerUpdate;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.fromjson.UpdateResult;
import com.minsait.onesait.platform.client.springboot.proxy.operations.Transaction.OperationType;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.extern.slf4j.Slf4j;

@Component("IoTBrokerUpdate")
@Slf4j
public class Update implements Operation {

	@Autowired
	private OperationUtil util;

	@Override
	public Object operation(Method method, Object[] args, ClientIoTBroker client, String ontology,
			Class<?> parametrizedType, boolean renewSession) throws SSAPConnectionException {
		final ObjectMapper mapper = new ObjectMapper();
		String instanceInString = null;
		String idInstance = null;
		try {
			final String tenantAnnotation = method.getAnnotation(IoTBrokerUpdate.class).tenant();
			if (args.length < 2 || !"".equals(tenantAnnotation) && args.length < 3) {
				log.error("We need at least 2 parameters: update(String identification,Object objectToUpdate)");
				throw new SSAPConnectionException(
						"We need at least 2 parameters: update(String identification,Object objectToUpdate)");
			}
			if (!method.getReturnType().isAssignableFrom(void.class)
					&& !method.getReturnType().isAssignableFrom(UpdateResult.class)) {
				log.error("@IoTBrokerUpdate must return void");
				throw new SSAPConnectionException("@IoTBrokerUpdate must return void");
			}
			// if (args[0].getClass() == String.class) {
			// idInstance = (String) args[0];
			// instanceInString = mapper.writeValueAsString(args[1]);
			// } else {
			// idInstance = (String) args[1];
			// instanceInString = mapper.writeValueAsString(args[0]);
			// }
			idInstance = OperationUtil.selectIdArgument(args, tenantAnnotation);
			instanceInString = mapper.writeValueAsString(OperationUtil.selectOntologyArgument(args));
			final String tenant = (String) util.parseSpEL(tenantAnnotation, args);
			if (method.getReturnType().getName().equals("void")) {
				client.init(tenant).update(ontology, instanceInString, idInstance);
				return null;
			} else {
				final JsonNode data = client.init(tenant).updateWithConfirmation(ontology, instanceInString,
						idInstance);
				final Object toReturn = new ObjectMapper().readValue(data.toString(), method.getReturnType());
				return toReturn;
			}

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in Update operation", e);
			throw new SSAPConnectionException("Error in Update", e);
		}
	}

	@Override
	public Object operationTx(Method method, Object[] args, Transaction tx, String ontology, Class<?> parametrizedType,
			boolean renewSession, OperationType operationType) throws SSAPConnectionException {
		return null;
	}

}
