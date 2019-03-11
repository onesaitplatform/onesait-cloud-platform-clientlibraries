/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
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
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.fromjson.DeleteResult;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.extern.slf4j.Slf4j;

@Component("IoTBrokerDelete")
@Slf4j
public class Delete implements Operation {

	@Autowired
	private OperationUtil util;

	public Object operation(Method method, Object[] args, ClientIoTBroker client, String ontology,
			Class<?> parametrizedType, boolean renewSession) throws SSAPConnectionException {
		String idInstance = null;
		try {
			if (args.length < 1) {
				log.error("We need at least 1 parameter: delete(String identification)");
				throw new SSAPConnectionException("We need at least 1 parameter: delete(String identification)");
			}
			if (!method.getReturnType().isAssignableFrom(void.class) && !method.getReturnType().isAssignableFrom(DeleteResult.class)) {
				log.error("@IoTBrokerDelete must return void");
				throw new SSAPConnectionException("@IoTBrokerDelete must return void");
			}
			if (args[0].getClass() == String.class) {
				idInstance = (String) args[0];
			} else {
				idInstance = (String) args[1];
			}
			if (method.getReturnType().getName().equals("void")) {
				client.init().delete(ontology, idInstance);
				return null;
			}else {
				JsonNode data = client.init().deleteWithConfirmation(ontology, idInstance);
				Object toReturn = new ObjectMapper().readValue(data.toString(), method.getReturnType());
				return toReturn;
			}

		} catch (SSAPConnectionException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in Delete operation", e);
			throw new SSAPConnectionException("Error in Delete", e);
		}
	}

}
