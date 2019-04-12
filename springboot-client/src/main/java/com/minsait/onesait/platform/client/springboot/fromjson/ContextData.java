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
package com.minsait.onesait.platform.client.springboot.fromjson;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class ContextData {

	// "contextData":{"clientPatform":"Ticketing
	// App","clientPatformInstance":"Ticketing App:
	// Web","clientConnection":"","clientSession":"6a3605d5-a1e5-4113-b5ac-2ac2138566d5","user":"administrator","timezoneId":"GMT","timestamp":"Wed
	// Apr 11 07:54:53 GMT 2018"}}

	@JsonIgnore
	private String clientPatform;
	@JsonIgnore
	private String clientPatformInstance;
	@JsonIgnore
	private String clientConnection;

	private String deviceTemplate;
	private String device;
	private String clientSession;
	private String user;
	private String timestamp;
	private String timezoneId;
	private String source;
	private long timestampMillis;
}