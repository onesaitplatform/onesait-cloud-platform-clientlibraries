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
import org.springframework.util.Assert;

import com.minsait.onesait.platform.client.Transaction;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ConnectionProperties;
import com.minsait.onesait.platform.client.springboot.dto.DeviceTokenDTO;
import com.minsait.onesait.platform.client.springboot.proxy.operations.OperationUtil;

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

	@Autowired
	private OperationUtil util;

	@Autowired
	private ClientIoTBroker iotBrokerClient;

	@Before("@annotation(ioTBrokerTransaction)")
	public void startTx(JoinPoint joinPoint, IoTBrokerTransaction ioTBrokerTransaction) {
		log.info("Start transaction.");
		if (TransactionContext.getTransactionContext() == null) {
			tx = new Transaction();
			final Properties prop = new Properties();
			prop.put(Transaction.DIGITAL_BROKER_REST_ENDPOINT, props.getUrlRestIoTBroker());
			prop.put(Transaction.CONNECTION_TYPE, Transaction.ConnectionType.REST.name());
			tx.configureConnection(prop);
			final String token = getToken(ioTBrokerTransaction, joinPoint.getArgs());
			final String transactionId = tx.start(token, props.getDeviceTemplate(), props.getDevice());
			if (transactionId != null) {
				final TransactionDTO ctx = new TransactionDTO(tx, transactionId, 1);
				TransactionContext.setTransactionContext(ctx);
			} else {
				log.error("Error starting transaction.");
			}
		} else {
			log.info("The transaction already exists. Transaction is NOT open again.");
			final Integer numTransaction = TransactionContext.getTransactionContext().getNumTransactions() + 1;
			TransactionContext.getTransactionContext().setNumTransactions(numTransaction);
		}
	}

	@AfterReturning("@annotation(IoTBrokerTransaction)")
	public void commitTx(JoinPoint jp) {
		log.info("Commit transaction.");
		if (TransactionContext.getTransactionContext().getNumTransactions() == 1) {
			final MethodSignature signature = (MethodSignature) jp.getSignature();
			final Method method = signature.getMethod();
			final IoTBrokerTransaction annotation = (IoTBrokerTransaction) method.getAnnotations()[0];
			tx.commit(annotation.lockOntologies());
			TransactionContext.clear();
		} else {
			log.info("This transaction is not the last one. Transaction is NOT commited already.");
			final Integer numTransaction = TransactionContext.getTransactionContext().getNumTransactions() - 1;
			TransactionContext.getTransactionContext().setNumTransactions(numTransaction);
		}
	}

	@AfterThrowing(pointcut = "@annotation(IoTBrokerTransaction)", throwing = "e")
	public void rollbackTx(Throwable e) {
		if (TransactionContext.getTransactionContext().getNumTransactions() == 1) {
			log.error("Rollback transaction. Exception ocurred: {}.", e);
			tx.rollback();
			TransactionContext.clear();
		} else {
			log.info("This transaction is not the last one. Transaction is NOT rollback already.");
			final Integer numTransaction = TransactionContext.getTransactionContext().getNumTransactions() - 1;
			TransactionContext.getTransactionContext().setNumTransactions(numTransaction);
		}

	}

	private String getToken(IoTBrokerTransaction annotation, Object args[]) {
		if (!props.isMultitenant()) {
			return props.getToken();
		} else {
			final String tenant = (String) util.parseSpEL(annotation.tenant(), args);
			final DeviceTokenDTO deviceToken = iotBrokerClient.getTokens().stream()
					.filter(t -> t.getTenant().equals(tenant)).findFirst().orElse(null);
			Assert.notNull(deviceToken, "No token found for tenant " + tenant);
			return deviceToken.getToken();
		}
	}

}
