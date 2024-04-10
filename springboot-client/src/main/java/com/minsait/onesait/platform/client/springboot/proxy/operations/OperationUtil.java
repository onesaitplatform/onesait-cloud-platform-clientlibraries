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
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDynamicQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDynamicRepository;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerParam;
import com.minsait.onesait.platform.client.springboot.proxy.exception.IoTBrokerDynamicQueryArgumentNotFound;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

@Component
public class OperationUtil {

	/**
	 * Mapea los atributos de los metodos a los parametros de la consulta
	 *
	 * @param method
	 * @param args
	 * @param query
	 * @return
	 */
	public String prepareQueryParameter(Method method, Object[] args, String query) throws Exception {
		final Parameter[] parameters = method.getParameters();
		IoTBrokerParam param = null;
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].getAnnotations().length > 0
					&& parameters[i].getAnnotations()[0] instanceof IoTBrokerParam) {
				param = (IoTBrokerParam) parameters[i].getAnnotations()[0];
				query = query.replace(param.value(), String.valueOf(args[i]));
			}
		}
		return query;
	}

	public String prepareQueryOntologyName(String query, String ontology) {
		query = query.replaceAll("\\$ONTOLOGY", ontology);
		return query;
	}

	public String prepareDynamicQuery(Method method, Object[] args) throws Exception {
		String query = null;
		final Parameter[] parameters = method.getParameters();
		final Map<String, String> arguments = new HashMap<String, String>();
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].getAnnotations().length > 0
					&& parameters[i].getAnnotations()[0] instanceof IoTBrokerDynamicQuery) {
				query = String.valueOf(args[i]);
			} else if (parameters[i].getAnnotations().length > 0
					&& parameters[i].getAnnotations()[0] instanceof IoTBrokerParam) {
				final IoTBrokerParam param = (IoTBrokerParam) parameters[i].getAnnotations()[0];
				arguments.put(param.value(), String.valueOf(args[i]));
			}
		}

		if (null == query) {
			throw new IoTBrokerDynamicQueryArgumentNotFound(
					"@IoTBrokerDynamicQuery annotation is required for one parameter");
		}

		for (final String argumentKey : arguments.keySet()) {
			query = query.replace(argumentKey, String.valueOf(arguments.get(argumentKey)));
		}

		return query;
	}

	public Optional<String> prepareDynamicRepository(Method method, Object[] args) throws Exception {
		final Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].getAnnotations().length > 0
					&& parameters[i].getAnnotations()[0] instanceof IoTBrokerDynamicRepository) {
				final String ontology = String.valueOf(args[i]);
				return Optional.of(ontology);
			}
		}

		return Optional.empty();

	}

	/**
	 * Preapara el objeto retorno ObjectId para ser devuelto en un Objeto o lista de
	 * Objetos
	 *
	 * @param message
	 * @return
	 * @throws Sofia2PersistenceException
	 */
	public String prepareObjectId(SSAPBodyReturnMessage message) {
		if (message.isOk()) {
			String data = message.getData().textValue();
			if (data.contains("ObjectId(")) {
				data = data.substring(1);
				data = "{" + data;
				data = data.replace("ObjectId(", "{\"$oid\":");
				data = data.replace(")", "}");
			}
			return data;
		} else {
			throw new SSAPConnectionException(
					"Error in invocation, ErrorCode:" + message.getErrorCode() + ",Error:" + message.getError());
		}
	}

	public Object parseSpEL(String expression, Object[] args) {
		if (StringUtils.isEmpty(expression) || args == null) {
			return "";
		}
		final ExpressionParser spELparser = new SpelExpressionParser();
		final StandardEvaluationContext context = new StandardEvaluationContext();
		for (int i = 0; i < args.length; i++) {
			context.setVariable("p" + i, args[i]);
		}
		final Object returnValue = spELparser.parseExpression(expression).getValue(context);
		if (returnValue == null) {
			return "";
		} else {
			return returnValue;
		}
	}

	public static Object selectOntologyArgument(Object[] args) {
		Object retVal = args[0];
		for (int i = 0; i < args.length; i++) {
			if (!(args[i] instanceof String)) {
				retVal = args[i];
			}
		}
		return retVal;
	}

	public static String selectIdArgument(Object[] args, String tenantExpression) {
		if (args != null && args.length > 0) {
			String returnVal = null;
			if (tenantExpression == null) {
				tenantExpression = "";
			}
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof String && !("#p" + i).equals(tenantExpression)) {
					returnVal = (String) args[i];
				}
			}
			return returnVal;
		} else {
			throw new IllegalArgumentException("No arguments to retrieve ID");
		}
	}
}
