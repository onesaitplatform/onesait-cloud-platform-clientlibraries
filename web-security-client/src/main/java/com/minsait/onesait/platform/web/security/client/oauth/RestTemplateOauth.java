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
package com.minsait.onesait.platform.web.security.client.oauth;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class RestTemplateOauth {

	@Value("${openplatform.api.auth.token.vertical:onesaitplatform}")
	private String vertical;

	@Bean("oauthRestTemplate")
	public RestOperations restTemplate() {
		final RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((r, b, e) -> {
			final URI uri = UriComponentsBuilder.fromHttpRequest(r).queryParam("vertical", vertical).build().toUri();
			final HttpRequest mr = new HttpRequestWrapper(r) {

				@Override
				public URI getURI() {
					return uri;
				}
			};

			return e.execute(mr, b);
		});
		return restTemplate;
	}
}
