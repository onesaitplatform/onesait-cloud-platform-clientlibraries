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
package com.minsait.onesait.platform.client.examples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleUtils {

	public static ExampleUtils getInstance() {
		return new ExampleUtils();
	}

	public String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())),
					Charset.forName("UTF-8"));

		} catch (final Exception e) {
			try {
				return new String(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						Charset.forName("UTF-8"));
			} catch (final IOException e1) {
				log.error("**********************************************");
				log.error("Error loading resource: " + name + ".Please check if this error affect your database");
				log.error(e.getMessage());
				return null;
			}
		}
	}

}
