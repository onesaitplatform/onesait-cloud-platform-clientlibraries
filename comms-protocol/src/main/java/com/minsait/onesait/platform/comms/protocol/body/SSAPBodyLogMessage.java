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

import java.awt.geom.Point2D;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPLogLevel;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPStatusType;

public class SSAPBodyLogMessage extends SSAPBodyMessage {

	private SSAPLogLevel level;
	private String message;
	private SSAPStatusType status;
	private JsonNode extraData;
	private Point2D.Double coordinates;
	private String commandId;
	private String tags;

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public Point2D.Double getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Point2D.Double coordinates) {
		this.coordinates = coordinates;
	}

	public SSAPLogLevel getLevel() {
		return level;
	}

	public void setLevel(SSAPLogLevel level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public JsonNode getExtraData() {
		return extraData;
	}

	public void setExtraData(JsonNode extraData) {
		this.extraData = extraData;
	}

	@Override
	public boolean isSessionKeyMandatory() {
		return true;
	}

	@Override
	public boolean isOntologyMandatory() {
		return false;
	}

	public SSAPStatusType getStatus() {
		return status;
	}

	public void setStatus(SSAPStatusType status) {
		this.status = status;
	}

}
