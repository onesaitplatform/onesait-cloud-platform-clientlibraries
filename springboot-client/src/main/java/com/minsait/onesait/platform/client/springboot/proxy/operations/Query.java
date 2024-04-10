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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.Transaction;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.fromjson.IntValue;
import com.minsait.onesait.platform.client.springboot.proxy.operations.Transaction.OperationType;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import lombok.extern.slf4j.Slf4j;

@Component("IoTBrokerQuery")
@Slf4j
public class Query implements Operation {

	@Autowired
	private OperationUtil util;

	@Override
	public Object operation(Method method, Object[] args, ClientIoTBroker client, String ontology,
			Class<?> parametrizedType, boolean renewSession) throws SSAPConnectionException {
		Object toReturn = null;
		final ObjectMapper mapper = new ObjectMapper();
		List<JsonNode> returnData = null;
		try {
			final IoTBrokerQuery queryAnnotation = (IoTBrokerQuery) method.getAnnotations()[0];
			String query = queryAnnotation.value();

			final Optional<String> dynamicRepository = util.prepareDynamicRepository(method, args);
			if (dynamicRepository.isPresent()) {
				ontology = dynamicRepository.get();
			}

			if (query.equals("")) {
				log.debug("Defined query: {}", query);
				query = util.prepareDynamicQuery(method, args);
				log.info("Prepared query: {}", query);
			} else {
				query = util.prepareQueryOntologyName(query, ontology);
				log.debug("Defined query: {}", query);
				query = util.prepareQueryParameter(method, args, query);
				log.info("Prepared query: {}", query);
			}

			final IoTBrokerQuery.QueryOf qOf = queryAnnotation.is();
			final String tenant = (String) util.parseSpEL(queryAnnotation.tenant(), args);
			if (qOf == IoTBrokerQuery.QueryOf.QUERY) {
				returnData = client.init(tenant).query(ontology, query, queryAnnotation.queryType());
			} else if (qOf == IoTBrokerQuery.QueryOf.UPDATE) {
				if (queryAnnotation.queryType() == SSAPQueryType.SQL) {
					returnData = new ArrayList<JsonNode>();
					returnData.add(client.init(tenant).updateSQL(ontology, query));
				} else {
					if (method.getReturnType().getName().equals("void")) {
						client.init(tenant).updateQuery(ontology, query);
					} else {
						final JsonNode data = client.init(tenant).updateQueryWithIds(ontology, query);
						returnData = new ArrayList<JsonNode>();
						returnData.add(data);
					}
				}

			} else if (qOf == IoTBrokerQuery.QueryOf.DELETE) {
				if (queryAnnotation.queryType() == SSAPQueryType.SQL) {
					returnData = new ArrayList<JsonNode>();
					returnData.add(client.init(tenant).updateSQL(ontology, query));
				} else {
					if (method.getReturnType().getName().equals("void")) {
						client.init(tenant).deleteQuery(ontology, query);
					} else {
						final JsonNode data = client.init(tenant).deleteQueryWithIds(ontology, query);
						returnData = new ArrayList<JsonNode>();
						returnData.add(data);
					}
				}
			} else {
				throw new SSAPConnectionException("QueryOf:" + qOf.name() + " not supported yet");
			}
			if (returnData == null) {
				return null;
			}

			if (method.getReturnType() == List.class) {
				final ParameterizedType stringListType = (ParameterizedType) method.getGenericReturnType();
				final Type t = stringListType.getActualTypeArguments()[0];

				JavaType type = null;
				if (parametrizedType != null && "T".equals(t.getTypeName())) {
					type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, parametrizedType);
				} else {
					type = mapper.getTypeFactory().constructCollectionType(ArrayList.class,
							(Class<?>) stringListType.getActualTypeArguments()[0]);
				}
				try {
					toReturn = mapper.readValue(returnData.toString(), type);
				} catch (final Exception e) {
					final List<JsonNode> returnDataByValue = new ArrayList<>();
					// it tries to obtain the data from value property
					for (final JsonNode jsonNode : returnData) {
						final JsonNode valueNode = jsonNode.get("value");
						// if there is not valueNode then the exception is propagated
						if (valueNode != null) {
							returnDataByValue.add(valueNode);
						} else {
							throw e;
						}
					}
					toReturn = mapper.readValue(returnDataByValue.toString(), type);
				}
				return toReturn;
			} else if (method.getReturnType().isArray()) {
				// TODO: develop two cases, with templated generics and without
			} else if (method.getReturnType() == Integer.class || method.getReturnType() == int.class) {
				final IntValue temp = mapper.readValue(returnData.get(0).toString(), IntValue.class);
				return temp.getValue();
			} else if (method.getReturnType().isAssignableFrom(void.class)) {
				return null;
			} else {
				if (returnData.size() > 1) {
					log.warn("Data with more than a JsonNode and you are only expecting one "
							+ method.getReturnType().getName() + ".Returning first.");
				}
				if (returnData.size() == 0) {
					return null;
				}
				try {
					if (method.getReturnType() == String.class) {
						toReturn = returnData.get(0).toString();
					} else {
						toReturn = mapper.readValue(returnData.get(0).toString(), method.getReturnType());
					}
				} catch (final Exception e) {
					toReturn = mapper.readValue(returnData.get(0).get("value").toString(), method.getReturnType());
				}
			}
			return toReturn;

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			log.error("Error in Query operation:", e);
			throw new SSAPConnectionException("Error in Query", e);
		}
	}

	public static class Transformer<T> {

		public T[] transform(Object o) {

			return (T[]) o;
		}
	}

	@Override
	public Object operationTx(Method method, Object[] args, Transaction tx, String ontology, Class<?> parametrizedType,
			boolean renewSession, OperationType operationType) throws SSAPConnectionException {
		return null;
	}

}
