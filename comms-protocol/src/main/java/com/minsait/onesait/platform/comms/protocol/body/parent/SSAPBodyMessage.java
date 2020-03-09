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
package com.minsait.onesait.platform.comms.protocol.body.parent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyCommandMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyCommitTransactionMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyEmptyMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyEmptySessionMandatoryMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyInsertMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLeaveMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyQueryMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodySubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUnsubscribeMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateMessage;

@JsonTypeInfo(use = Id.NAME)
@JsonSubTypes({ @JsonSubTypes.Type(SSAPBodyDeleteByIdMessage.class), @JsonSubTypes.Type(SSAPBodyDeleteMessage.class),
		@JsonSubTypes.Type(SSAPBodyEmptyMessage.class), @JsonSubTypes.Type(SSAPBodyEmptySessionMandatoryMessage.class),
		@JsonSubTypes.Type(SSAPBodyInsertMessage.class), @JsonSubTypes.Type(SSAPBodyJoinMessage.class),
		@JsonSubTypes.Type(SSAPBodyLeaveMessage.class), @JsonSubTypes.Type(SSAPBodyQueryMessage.class),
		@JsonSubTypes.Type(SSAPBodyReturnMessage.class), @JsonSubTypes.Type(SSAPBodyUpdateByIdMessage.class),
		@JsonSubTypes.Type(SSAPBodyUpdateMessage.class), @JsonSubTypes.Type(SSAPBodySubscribeMessage.class),
		@JsonSubTypes.Type(SSAPBodyUnsubscribeMessage.class), @JsonSubTypes.Type(SSAPBodyIndicationMessage.class),
		@JsonSubTypes.Type(SSAPBodyCommandMessage.class), @JsonSubTypes.Type(SSAPBodyCommitTransactionMessage.class) })
public abstract class SSAPBodyMessage {
	@JsonIgnore
	public abstract boolean isSessionKeyMandatory();

	@JsonIgnore
	public abstract boolean isOntologyMandatory();

}
