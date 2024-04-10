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
package com.minsait.onesait.platform.client.springboot.proxy;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDelete;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerInsert;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerUpdate;
import com.minsait.onesait.platform.client.springboot.aspect.transaction.TransactionContext;
import com.minsait.onesait.platform.client.springboot.aspect.transaction.TransactionDTO;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.proxy.operations.Operation;
import com.minsait.onesait.platform.client.springboot.proxy.operations.Transaction.OperationType;

public class InvocationHandler implements java.lang.reflect.InvocationHandler {

	private ClientIoTBroker client;

	private Operation query;
	private Operation insert;
	private Operation delete;
	private Operation update;
	private Operation transaction;

	private ApplicationContext context;
	private String ontology;
	private Class<?> parametrizedType;

	/**
	 * Constructor
	 * 
	 * @param context Contexto de Spring
	 */
	public InvocationHandler(String ontology, ApplicationContext context) {
		this(ontology, context, null);
	}

	public InvocationHandler(String ontology, ApplicationContext context, Class<?> ptype) {
		this.ontology = ontology;
		this.context = context;
		parametrizedType = ptype;
	}

	/**
	 * Inicializador de la clase, recupera los objetos del contexto de Spring
	 */
	private void init() {
		if (client == null) {
			client = context.getBean(ClientIoTBroker.class);
		}
		if (query == null) {
			query = (Operation) context.getBean("IoTBrokerQuery");
		}
		if (insert == null) {
			insert = (Operation) context.getBean("IoTBrokerInsert");
		}
		if (delete == null) {
			delete = (Operation) context.getBean("IoTBrokerDelete");
		}
		if (update == null) {
			update = (Operation) context.getBean("IoTBrokerUpdate");
		}
		if (transaction == null) {
			transaction = (Operation) context.getBean("IoTBrokerTransaction");
		}
	}

	/**
	 * En funcion de la anotacion del metodo delega en la operacion adecuada. En el
	 * caso de existir el objeto TransactionContext, implica que las operaciones
	 * INSERT, UPDATE, DELETE se asociarán con la transacción correspondiente. En el
	 * caso del método QUERY siempre irá sin transacción.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws java.lang.Throwable {
		Object toReturn = null;
		init();
		TransactionDTO transactionContext = TransactionContext.getTransactionContext();
		if (method.getAnnotations()[0] instanceof IoTBrokerQuery) {
			toReturn = query.operation(method, args, client, ontology, parametrizedType, false);
		} else if (method.getAnnotations()[0] instanceof IoTBrokerInsert && transactionContext == null) {
			toReturn = insert.operation(method, args, client, ontology, parametrizedType, false);
		} else if (method.getAnnotations()[0] instanceof IoTBrokerDelete && transactionContext == null) {
			toReturn = delete.operation(method, args, client, ontology, parametrizedType, false);
		} else if (method.getAnnotations()[0] instanceof IoTBrokerUpdate && transactionContext == null) {
			toReturn = update.operation(method, args, client, ontology, parametrizedType, false);
		} else if (method.getAnnotations()[0] instanceof IoTBrokerInsert && transactionContext != null) {
			toReturn = transaction.operationTx(method, args, transactionContext.getTx(), ontology, parametrizedType,
					false, OperationType.INSERT);
		} else if (method.getAnnotations()[0] instanceof IoTBrokerDelete && transactionContext != null) {
			toReturn = transaction.operationTx(method, args, transactionContext.getTx(), ontology, parametrizedType,
					false, OperationType.DELETE);
		} else if (method.getAnnotations()[0] instanceof IoTBrokerUpdate && transactionContext != null) {
			toReturn = transaction.operationTx(method, args, transactionContext.getTx(), ontology, parametrizedType,
					false, OperationType.UPDATE);
		}

		return toReturn;
	}
}
