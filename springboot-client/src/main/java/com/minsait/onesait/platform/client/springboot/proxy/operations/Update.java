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
/*******************************************************************************
 * Indra Sistemas, S.A.
 * 2013 - 2017  SPAIN
 *
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.client.springboot.proxy.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.fromjson.UpdateResult;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import javax.persistence.Version;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Component("IoTBrokerUpdate")
@Slf4j
public class Update implements Operation {

	protected static final String QUERY_BY_OBJECT_ID="select _id, c.* from %s as c where _id = %s";
	@Autowired
	private OperationUtil util;

	private Field getVersionField(Class c){
		List<Field> fieldsWithVersion=FieldUtils.getFieldsListWithAnnotation(c, Version.class);
		if(fieldsWithVersion.size()==0){
			return null;
		}else if(fieldsWithVersion.size()==1) {
			return fieldsWithVersion.get(0);
		}else{
			throw new SSAPConnectionException("Entity only can have one Version Field");
		}
	}

	public Object operation(Method method, Object[] args, ClientIoTBroker client, String ontology,
			Class<?> parametrizedType, boolean renewSession) throws SSAPConnectionException {
		ObjectMapper mapper = new ObjectMapper();
		String idInstance ;
		Object entityInstance;
		try {
			if (args.length < 2) {
				log.error("We need at least 2 parameters: update(String identification,Object objectToUpdate)");
				throw new SSAPConnectionException(
						"We need at least 2 parameters: update(String identification,Object objectToUpdate)");
			}
			if (!method.getReturnType().isAssignableFrom(void.class) && !method.getReturnType().isAssignableFrom(UpdateResult.class)) {
				log.error("@IoTBrokerUpdate must return void");
				throw new SSAPConnectionException("@IoTBrokerUpdate must return void");
			}
			if (args[0].getClass() == String.class) {
				idInstance = (String) args[0];
				entityInstance = args[1];
			} else {
				idInstance = (String) args[1];
				entityInstance = args[0];
			}

			Field versionField= getVersionField(entityInstance.getClass());
			if(versionField!=null){
				versionField.setAccessible(true);
				List<JsonNode> results=client.init().query(ontology, String.format(QUERY_BY_OBJECT_ID, ontology, idInstance), SSAPQueryType.SQL);
				if(results.size()>0){
					Object theReturn=new ObjectMapper().readValue(results.get(0).toString(), entityInstance.getClass());
					if(versionField.get(entityInstance).equals(versionField.get(theReturn))){
						log.debug("Version is the same");
					}else{
						throw new OptimisticLockException("Bad version value in updated object");
					}

				}
			}
			if (method.getReturnType().getName().equals("void")) {
				client.init().update(ontology, mapper.writeValueAsString(entityInstance), idInstance);
				return null;
			}else {
				JsonNode data = client.init().updateWithConfirmation(ontology, mapper.writeValueAsString(entityInstance), idInstance);
				return new ObjectMapper().readValue(data.toString(), method.getReturnType());
			}


		} catch (SSAPConnectionException | OptimisticLockException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in Update operation", e);
			throw new SSAPConnectionException("Error in Update", e);
		}
	}

}
