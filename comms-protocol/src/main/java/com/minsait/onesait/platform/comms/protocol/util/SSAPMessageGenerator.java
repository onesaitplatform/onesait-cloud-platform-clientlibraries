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
package com.minsait.onesait.platform.comms.protocol.util;

import java.awt.geom.Point2D;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyInsertMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLeaveMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLogMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyQueryMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodySubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUnsubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPLogLevel;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryResultFormat;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPStatusType;

public class SSAPMessageGenerator {

	public static SSAPMessage<SSAPBodyReturnMessage> generateResponseErrorMessage(
			SSAPMessage<? extends SSAPBodyMessage> message, SSAPErrorCode errorCode, String errorMessage) {
		SSAPMessage<SSAPBodyReturnMessage> m = null;
		m = new SSAPMessage<>();
		m.setDirection(SSAPMessageDirection.ERROR);
		m.setMessageId(message.getMessageId());
		m.setSessionKey(message.getSessionKey());
		m.setBody(new SSAPBodyReturnMessage());
		m.getBody().setOk(false);
		m.getBody().setErrorCode(errorCode);
		m.getBody().setError(errorMessage);

		return m;
	}

	public static SSAPMessage<SSAPBodyJoinMessage> generateRequestJoinMessage(String token, String deviceTemplate,
			String device, String sessionKey, String tags, JsonNode commandConfiguration) {
		final SSAPMessage<SSAPBodyJoinMessage> join = new SSAPMessage<>();
		join.setDirection(SSAPMessageDirection.REQUEST);
		join.setMessageType(SSAPMessageTypes.JOIN);
		final SSAPBodyJoinMessage body = new SSAPBodyJoinMessage();
		body.setDeviceTemplate(deviceTemplate);
		body.setDevice(device);
		body.setToken(token);
		body.setDeviceConfiguration(commandConfiguration);
		body.setTags(tags);
		join.setBody(body);
		join.setSessionKey(sessionKey);

		return join;

	}

	public static SSAPMessage<SSAPBodySubscribeMessage> generateRequestSubscriptionMessage(String subscription,
			String queryValue, String callback, String sessionKey, String clientId) {

		final SSAPMessage<SSAPBodySubscribeMessage> subscriptionMessage = new SSAPMessage<>();
		subscriptionMessage.setSessionKey(sessionKey);
		subscriptionMessage.setDirection(SSAPMessageDirection.REQUEST);
		subscriptionMessage.setMessageType(SSAPMessageTypes.SUBSCRIBE);
		final SSAPBodySubscribeMessage body = new SSAPBodySubscribeMessage();
		body.setCallback(callback);
		body.setQueryValue(queryValue);
		body.setSubscription(subscription);
		body.setClientId(clientId);
		subscriptionMessage.setBody(body);

		return subscriptionMessage;
	}

	public static SSAPMessage<SSAPBodyUnsubscribeMessage> generateRequestUnsubscribeMessage(String sessionKey,
			String subscriptionId) {
		final SSAPMessage<SSAPBodyUnsubscribeMessage> unsubscribe = new SSAPMessage<SSAPBodyUnsubscribeMessage>();
		unsubscribe.setSessionKey(sessionKey);

		final SSAPBodyUnsubscribeMessage body = new SSAPBodyUnsubscribeMessage();
		body.setSubscriptionId(subscriptionId);

		unsubscribe.setBody(body);
		unsubscribe.setDirection(SSAPMessageDirection.REQUEST);
		unsubscribe.setMessageType(SSAPMessageTypes.UNSUBSCRIBE);
		return unsubscribe;
	}

	public static SSAPMessage<SSAPBodyIndicationMessage> generateResponseIndicationMessage(String subscriptionId,
			String data, String sessionKey) {
		final SSAPMessage<SSAPBodyIndicationMessage> indication = new SSAPMessage<SSAPBodyIndicationMessage>();

		final SSAPBodyIndicationMessage body = new SSAPBodyIndicationMessage();
		body.setData(data);
		body.setSubsciptionId(subscriptionId);

		indication.setBody(body);
		indication.setDirection(SSAPMessageDirection.RESPONSE);
		indication.setMessageType(SSAPMessageTypes.INDICATION);
		indication.setSessionKey(sessionKey);
		return indication;
	}

	public static SSAPMessage<SSAPBodyLogMessage> generateLogMessage(String sessionKey, double latitude,
			double longitude, String logLevel, String status, String commandId, String message) {
		final SSAPMessage<SSAPBodyLogMessage> logMessage = new SSAPMessage<SSAPBodyLogMessage>();
		final SSAPBodyLogMessage body = new SSAPBodyLogMessage();
		logMessage.setDirection(SSAPMessageDirection.REQUEST);
		logMessage.setMessageType(SSAPMessageTypes.LOG);
		logMessage.setSessionKey(sessionKey);
		final Point2D.Double coordinates = new Point2D.Double(latitude, longitude);
		coordinates.setLocation(coordinates);
		body.setCoordinates(coordinates);
		body.setLevel(SSAPLogLevel.valueOf(logLevel));
		body.setMessage(message);
		body.setCommandId(commandId);
		body.setStatus(SSAPStatusType.valueOf(status));
		logMessage.setBody(body);

		return logMessage;
	}

	public static SSAPMessage<SSAPBodyQueryMessage> generateQueryMessage(String sessionKey, String ontology,
			String query, SSAPQueryType queryType) {
		final SSAPMessage<SSAPBodyQueryMessage> update = new SSAPMessage<SSAPBodyQueryMessage>();
		final SSAPBodyQueryMessage body = new SSAPBodyQueryMessage();
		update.setDirection(SSAPMessageDirection.REQUEST);
		update.setMessageType(SSAPMessageTypes.QUERY);
		update.setSessionKey(sessionKey);
		body.setOntology(ontology);
		body.setQuery(query);
		body.setQueryType(queryType);
		body.setResultFormat(SSAPQueryResultFormat.JSON);
		update.setBody(body);

		return update;
	}

	public static SSAPMessage<SSAPBodyInsertMessage> generateRequestInsertMessage(String sessionKey, String ontology,
			JsonNode data) {
		final SSAPMessage<SSAPBodyInsertMessage> insert = new SSAPMessage<SSAPBodyInsertMessage>();
		final SSAPBodyInsertMessage body = new SSAPBodyInsertMessage();
		insert.setDirection(SSAPMessageDirection.REQUEST);
		insert.setMessageType(SSAPMessageTypes.INSERT);
		insert.setSessionKey(sessionKey);
		body.setOntology(ontology);
		body.setData(data);
		insert.setBody(body);

		return insert;
	}

	public static SSAPMessage<SSAPBodyUpdateByIdMessage> generateUpdateByIdMessage(String sessionKey, String ontology,
			String id, JsonNode data) {
		final SSAPMessage<SSAPBodyUpdateByIdMessage> update = new SSAPMessage<SSAPBodyUpdateByIdMessage>();
		final SSAPBodyUpdateByIdMessage body = new SSAPBodyUpdateByIdMessage();
		update.setDirection(SSAPMessageDirection.REQUEST);
		update.setMessageType(SSAPMessageTypes.UPDATE_BY_ID);
		update.setSessionKey(sessionKey);
		body.setOntology(ontology);
		body.setData(data);
		body.setId(id);
		update.setBody(body);

		return update;
	}

	public static SSAPMessage<SSAPBodyUpdateMessage> generateUpdateByNativeQueryMessage(String sessionKey,
			String ontology, String query) {
		final SSAPMessage<SSAPBodyUpdateMessage> update = new SSAPMessage<SSAPBodyUpdateMessage>();
		final SSAPBodyUpdateMessage body = new SSAPBodyUpdateMessage();
		update.setDirection(SSAPMessageDirection.REQUEST);
		update.setMessageType(SSAPMessageTypes.UPDATE);
		update.setSessionKey(sessionKey);
		body.setOntology(ontology);
		body.setQuery(query);
		update.setBody(body);

		return update;
	}

	public static SSAPMessage<SSAPBodyDeleteMessage> generateDeteteByNativeQueryMessage(String sessionKey,
			String ontology, String query) {
		final SSAPMessage<SSAPBodyDeleteMessage> delete = new SSAPMessage<SSAPBodyDeleteMessage>();
		final SSAPBodyDeleteMessage body = new SSAPBodyDeleteMessage();
		delete.setDirection(SSAPMessageDirection.REQUEST);
		delete.setMessageType(SSAPMessageTypes.DELETE);
		delete.setSessionKey(sessionKey);
		body.setOntology(ontology);
		body.setQuery(query);
		delete.setBody(body);

		return delete;
	}

	public static SSAPMessage<SSAPBodyDeleteByIdMessage> generateDeteteByIdMessage(String sessionKey, String ontology,
			String id) {
		final SSAPMessage<SSAPBodyDeleteByIdMessage> delete = new SSAPMessage<SSAPBodyDeleteByIdMessage>();
		final SSAPBodyDeleteByIdMessage body = new SSAPBodyDeleteByIdMessage();
		delete.setDirection(SSAPMessageDirection.REQUEST);
		delete.setMessageType(SSAPMessageTypes.DELETE_BY_ID);
		delete.setSessionKey(sessionKey);
		body.setOntology(ontology);
		body.setId(id);
		delete.setBody(body);

		return delete;
	}

	public static SSAPMessage<SSAPBodyLeaveMessage> generateRequestLeaveMessage(String sessionKey) {

		final SSAPMessage<SSAPBodyLeaveMessage> leave = new SSAPMessage<SSAPBodyLeaveMessage>();
		leave.setDirection(SSAPMessageDirection.REQUEST);
		leave.setMessageType(SSAPMessageTypes.LEAVE);
		leave.setSessionKey(sessionKey);
		leave.setBody(new SSAPBodyLeaveMessage());

		return leave;
	}
}
