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

import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyOntologyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryResultFormat;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;

public class SSAPBodyQueryMessage extends SSAPBodyOntologyMessage {

	private String query;
	private SSAPQueryType queryType;
	private SSAPQueryResultFormat resultFormat;
	private long cacheTime;
	private String tags;

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public SSAPQueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(SSAPQueryType queryType) {
		this.queryType = queryType;
	}

	public SSAPQueryResultFormat getResultFormat() {
		return resultFormat;
	}

	public void setResultFormat(SSAPQueryResultFormat resultFormat) {
		this.resultFormat = resultFormat;
	}

	public long getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}

	@Override
	public boolean isSessionKeyMandatory() {
		return true;
	}

	@Override
	public boolean isOntologyMandatory() {
		return true;
	}

}
