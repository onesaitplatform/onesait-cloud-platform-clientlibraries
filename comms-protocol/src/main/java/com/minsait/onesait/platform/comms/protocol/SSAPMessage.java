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
package com.minsait.onesait.platform.comms.protocol;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyEmptyMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;

public final class SSAPMessage<T extends SSAPBodyMessage> implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String messageId;
	protected String sessionKey;
	// protected String ontology;
	protected SSAPMessageDirection direction;
	protected SSAPMessageTypes messageType;

	@JsonIgnore
	private boolean includeIds;

	@SuppressWarnings("unchecked")
	protected T body = (T) new SSAPBodyEmptyMessage();

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	// public String getOntology() {
	// return ontology;
	// }
	// public void setOntology(String ontology) {
	// this.ontology = ontology;
	// }
	@JsonProperty(required = true)
	public SSAPMessageDirection getDirection() {
		return direction;
	}

	@JsonProperty(required = true)
	public void setDirection(SSAPMessageDirection direction) {
		this.direction = direction;
	}

	@JsonProperty(required = true)
	public SSAPMessageTypes getMessageType() {
		return messageType;
	}

	@JsonProperty(required = true)
	public void setMessageType(SSAPMessageTypes messageType) {
		this.messageType = messageType;
	}

	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}

	@JsonIgnore
	public boolean includeIds() {
		return includeIds;
	}

	@JsonIgnore
	public void setIncludeIds(boolean ids) {
		this.includeIds = ids;
	}

}
