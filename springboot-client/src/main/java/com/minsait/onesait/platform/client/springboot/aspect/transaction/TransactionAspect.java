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
package com.minsait.onesait.platform.client.springboot.aspect.transaction;

import java.lang.reflect.Method;
import java.util.Properties;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.client.Transaction;
import com.minsait.onesait.platform.client.Transaction.ConnectionType;
import com.minsait.onesait.platform.client.Transaction.RestProperty;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ConnectionProperties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Scope("prototype")
@EnableConfigurationProperties(ConnectionProperties.class)
@Slf4j
public class TransactionAspect {

	@Getter
	private Transaction tx;

	@Autowired
	private ConnectionProperties props;

	@Before("@annotation(IoTBrokerTransaction)")
	public void startTx() {
		log.info("Start transaction.");
		if (TransactionContext.getTransactionContext() == null) {
			tx = new Transaction();
			Properties prop = new Properties();
			prop.put(RestProperty.URL.name(), props.getUrlRestIoTBroker());
			tx.configureConnection(ConnectionType.REST, prop);

			String transactionId = tx.start(props.getToken(), props.getDeviceTemplate(), props.getDevice());
			if (transactionId != null) {
				TransactionDTO ctx = new TransactionDTO(tx, transactionId, 1);
				TransactionContext.setTransactionContext(ctx);
			} else {
				log.error("Error starting transaction.");
			}
		} else {
			log.info("The transaction already exists. Transaction is NOT open again.");
			Integer numTransaction = TransactionContext.getTransactionContext().getNumTransactions() + 1;
			TransactionContext.getTransactionContext().setNumTransactions(numTransaction);
		}
	}

	@AfterReturning("@annotation(IoTBrokerTransaction)")
	public void commitTx(JoinPoint jp) {
		log.info("Commit transaction.");
		if (TransactionContext.getTransactionContext().getNumTransactions() == 1) {
			MethodSignature signature = (MethodSignature) jp.getSignature();
			Method method = signature.getMethod();
			IoTBrokerTransaction annotation = (IoTBrokerTransaction) method.getAnnotations()[0];
			tx.commit(annotation.lockOntologies());
			TransactionContext.clear();
		} else {
			log.info("This transaction is not the last one. Transaction is NOT commited already.");
			Integer numTransaction = TransactionContext.getTransactionContext().getNumTransactions() - 1;
			TransactionContext.getTransactionContext().setNumTransactions(numTransaction);
		}
	}

	@AfterThrowing(pointcut = "@annotation(IoTBrokerTransaction)", throwing = "e")
	public void rollbackTx(Throwable e) {
		log.error("Rollback transaction. Exception ocurred: {}.", e);
		tx.rollback();
		TransactionContext.clear();
	}

}
