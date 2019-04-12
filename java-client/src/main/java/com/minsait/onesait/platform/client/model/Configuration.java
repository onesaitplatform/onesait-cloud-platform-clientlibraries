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
package com.minsait.onesait.platform.client.model;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.client.enums.ConfigurationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

	private String id;
	private String username;
	@NonNull
	private ConfigurationType type;
	@NonNull
	private String description;
	@NonNull
	private String suffix;
	@NonNull
	private String environment;

	private Map<String, Object> yml;

	@SuppressWarnings("unchecked")
	@JsonSetter("yml")
	public void setYml(String ymlPlain) {
		final Yaml yaml = new Yaml();
		yml = (Map<String, Object>) yaml.load(ymlPlain);
	}

	@JsonGetter("yml")
	public String getYml() {
		final Yaml yaml = new Yaml();
		return yaml.dump(yml);
	}

	public String getYmlAsString() {
		final Yaml yaml = new Yaml();
		return yaml.dump(yml);
	}

	@SuppressWarnings("unchecked")
	public void setYmlFromString(String yml) {
		final Yaml yaml = new Yaml();
		this.yml = (Map<String, Object>) yaml.load(yml);
	}
}
