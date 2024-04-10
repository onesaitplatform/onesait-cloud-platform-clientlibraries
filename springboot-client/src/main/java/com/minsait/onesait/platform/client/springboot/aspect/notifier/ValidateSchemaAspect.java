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
import java.lang.reflect.Parameter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.client.springboot.autoconfigure.NotifierClientConnection;

@Aspect
@Order(1)
@Component
@ConditionalOnExpression(value = "${onesaitplatform.notifierclient.enabled:false}")
public class ValidateSchemaAspect {

	@Autowired
	private NotifierClientConnection notifierConnection;

	@Pointcut(value = "execution(* com.minsait..*(.., @OPValidateSchema (*), ..))")
	public void validateSchemaParams() {
	}

	@Pointcut(value = "execution(* com.minsait..*(..))")
	public void validateSchemaEntity() {
	}

	@AfterReturning("validateSchemaParams()")
	public void validateSchemaParam(JoinPoint joinPoint) {

		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method methodReflection = signature.getMethod();

		Parameter[] parameters = methodReflection.getParameters();
		Object[] signatureArgs = joinPoint.getArgs();

		for (int i = 0; i < signatureArgs.length; i++) {
			boolean isPresent = parameters[i].isAnnotationPresent(OPValidateSchema.class);
			if (isPresent) {
				notifierConnection.init().validateSchema(signatureArgs[i]);
			}
		}
	}

	@AfterReturning("validateSchemaEntity()")
	public void validateSchemaEntity(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method methodReflection = signature.getMethod();

		Parameter[] parameters = methodReflection.getParameters();
		Object[] signatureArgs = joinPoint.getArgs();

		for (int i = 0; i < signatureArgs.length; i++) {
			boolean isPresent = parameters[i].getClass().isAnnotationPresent(OPValidateSchema.class);
			if (isPresent) {
				notifierConnection.init().validateSchema(signatureArgs[i]);
			}
		}
	}

}
