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
package com.minsait.onesait.platform.client.springboot.autoconfigure;

import com.minsait.onesait.platform.client.NotifierClient;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

public class NotifierClientConnection {

	private NotifierClient client;

	private String server;

	private String username;

	private String password;

	private String apikey;

	private boolean sslverify;

	public void setConnectionParameters(String server, String username, String password, boolean sslverify) {
		this.server = server;
		this.username = username;
		this.password = password;
		this.sslverify = sslverify;
	}

	public void setConnectionParameters(String server, String apikey, boolean sslverify) {
		this.server = server;
		this.apikey = apikey;
		this.sslverify = sslverify;
	}

	public NotifierClient init() throws SSAPConnectionException {
		synchronized (this) {
			if (client == null && apikey == null) {
				client = new NotifierClient(server, username, password, sslverify);
			}

			if (client == null && apikey != null) {
				client = new NotifierClient(server, apikey, sslverify);
			}
		}
		return this.client;
	}
}
