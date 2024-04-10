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
package com.minsait.onesait.platform.comms.protocol.body;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;

public class SSAPBodyReturnMessage extends SSAPBodyMessage {

	private boolean ok = true;
	private String error;
	private SSAPErrorCode errorCode;
	private JsonNode data;

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public SSAPErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(SSAPErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public boolean isSessionKeyMandatory() {
		return false;
	}

	@Override
	public boolean isOntologyMandatory() {
		return false;
	}

	public JsonNode getData() {
		return data;
	}

	public void setData(JsonNode data) {
		this.data = data;
	}

}
