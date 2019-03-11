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
package com.minsait.onesait.platform.client.springboot.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IoTBrokerQuery {

	public enum QueryOf {
		QUERY, UPDATE, INSERT, DELETE
	}

	/**
	 * Es la consulta que se quiere ejecutar sobre la plataforma Sofia2 (select *
	 * from Alarma where Alarma.tipo=$tipo)
	 * 
	 * @return String
	 */
	String value() default "";

	/**
	 * QueryType: NATIVE/SQL
	 * 
	 * @return SSAPQueryType
	 */
	SSAPQueryType queryType() default SSAPQueryType.SQL;

	QueryOf is() default QueryOf.QUERY;

}
