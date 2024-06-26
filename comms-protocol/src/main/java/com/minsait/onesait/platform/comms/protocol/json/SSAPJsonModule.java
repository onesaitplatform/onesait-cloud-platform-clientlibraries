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
package com.minsait.onesait.platform.comms.protocol.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.Module;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.json.version.one.OneSSAPBodyMessageMixin;
import com.minsait.onesait.platform.comms.protocol.json.version.one.OneSSAPMessageMixin;

public class SSAPJsonModule extends Module {
	private static final String NAME = "JACKSON_MODULE";
	private static final VersionUtil VERSION_UTIL = new VersionUtil() {
	};

	@Override
	public String getModuleName() {
		return NAME;
	}

	@Override
	public Version version() {
		return VERSION_UTIL.version();
	}

	@Override
	public void setupModule(SetupContext context) {
		context.setMixInAnnotations(SSAPBodyMessage.class, OneSSAPBodyMessageMixin.class);
		context.setMixInAnnotations(SSAPMessage.class, OneSSAPMessageMixin.class);
	}

}
