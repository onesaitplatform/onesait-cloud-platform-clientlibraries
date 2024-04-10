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
package com.minsait.onesait.platform.client.springboot.fromjson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.minsait.onesait.platform.client.springboot.aspect.transaction.TransactionAspect;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateDeserializer extends StdDeserializer<Date> implements DateParserConstants {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DateDeserializer() {
		this(null);
	}

	public DateDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		final String date = node.textValue();

		for (String DATE_FORMAT : DATE_FORMATS) {
			try {
				return new SimpleDateFormat(DATE_FORMAT).parse(date);
			} catch (ParseException e) {
				log.error("Error parsing dates: ", e.getMessage());
			}
		}
		throw new JsonParseException(jp,
				"Unparseable date: \"" + date + "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
	}
}
