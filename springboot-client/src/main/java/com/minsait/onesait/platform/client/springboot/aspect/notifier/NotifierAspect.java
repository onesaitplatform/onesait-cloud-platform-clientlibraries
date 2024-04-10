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
package com.minsait.onesait.platform.client.springboot.aspect.notifier;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.enums.OperationType;
import com.minsait.onesait.platform.client.enums.QueryType;
import com.minsait.onesait.platform.client.model.Notification;
import com.minsait.onesait.platform.client.springboot.autoconfigure.NotifierClientConnection;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Order(2)
@Component
@ConditionalOnExpression(value = "${onesaitplatform.notifierclient.enabled:false}")
@Slf4j
public class NotifierAspect {

	@Autowired
	private NotifierClientConnection notifierConnection;
	@Autowired
	private ObjectMapper mapper;

	private final ExpressionParser spELparser = new SpelExpressionParser();

	@AfterReturning(pointcut = "@annotation(OPNotifierOperation)", returning = "result")
	public void notifierOperation(JoinPoint joinPoint, Object result) {

		final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		final Method methodReflection = signature.getMethod();

		final OPNotifierOperation annotationValue = methodReflection.getAnnotation(OPNotifierOperation.class);

		final Object[] signatureArgs = joinPoint.getArgs();

		final Notification notification = Notification.builder().ontology(annotationValue.ontology())
				.queryType(QueryType.NATIVE).operation(annotationValue.operationType()).build();

		if (signatureArgs.length == 1 && annotationValue.operationType().equals(OperationType.INSERT)) {
			setPayload(notification, signatureArgs, annotationValue, 0);
		}

		if (signatureArgs.length == 1 && annotationValue.operationType().equals(OperationType.DELETE)) {
			notification.setId((String) spELparser.parseExpression(annotationValue.id())
					.getValue(getEvaluationContext(signatureArgs)));
		}

		if (signatureArgs.length >= 1 && annotationValue.operationType().equals(OperationType.UPDATE)) {
			notification.setId((String) spELparser.parseExpression(annotationValue.id())
					.getValue(getEvaluationContext(signatureArgs)));
			setPayload(notification, signatureArgs, annotationValue, 1);

		}

		if (annotationValue.async()) {
			notifierConnection.init().notifyAsync(notification);
		} else {
			notifierConnection.init().notify(notification);
		}
	}

	private StandardEvaluationContext getEvaluationContext(Object[] args) {
		final StandardEvaluationContext context = new StandardEvaluationContext();
		for (int i = 0; i < args.length; i++) {
			context.setVariable("p" + i, args[i]);
		}
		return context;
	}

	private void setPayload(Notification notification, Object[] args, OPNotifierOperation annotationValue,
			int defaultArgPosition) {
		Object payload = null;
		if (!StringUtils.isEmpty(annotationValue.payload())
				&& !annotationValue.id().equals(annotationValue.payload())) {
			payload = spELparser.parseExpression(annotationValue.payload()).getValue(getEvaluationContext(args));
		} else {
			payload = args[defaultArgPosition];
		}
		if (payload instanceof String) {
			notification.setPayload((String) payload);
		} else {
			try {
				notification.setPayload(mapper.writeValueAsString(payload));
			} catch (final JsonProcessingException e) {
				log.debug("could not write object as String, fallback to empty");
				notification.setPayload(null);
			}
		}

	}

	@Around(value = "@annotation(OPNotifierOperation)")
	public Object around(ProceedingJoinPoint proceedingJointPoint) throws Throwable {
		return proceedingJointPoint.proceed();
	}
}
