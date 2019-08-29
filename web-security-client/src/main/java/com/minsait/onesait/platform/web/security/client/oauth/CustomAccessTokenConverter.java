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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessTokenConverter extends DefaultAccessTokenConverter {

	private final static String JWT_PRINCIPAL_KEY = "principal";
	private final static String TOKEN_CONVERTER_PRINCIPAL_KEY = "principal";

	@Override
	public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
		Map<String, Object> preparedClaims = new HashMap<String, Object>();
		for (Entry entry : claims.entrySet()) {
			preparedClaims.put((String) entry.getKey(), entry.getValue());
		}

		String principal = (String) claims.get(JWT_PRINCIPAL_KEY);
		if (null != principal) {
			preparedClaims.put(TOKEN_CONVERTER_PRINCIPAL_KEY, (String) claims.get(JWT_PRINCIPAL_KEY));
		}

		OAuth2Authentication authentication = super.extractAuthentication(preparedClaims);
		authentication.setDetails(claims);
		return authentication;
	}

}
