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
package com.minsait.onesait.platform.client.utils;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.MDC;

import com.minsait.onesait.platform.client.TimeOutConfig;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

import okhttp3.OkHttpClient;

public class OkHttpClientUtil {

	public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";
	public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
	public static final String AUTHORIZATION_STR = "Authorization";
	public static final String API_KEY = "X-OP-APIKey";

	public static OkHttpClient getSafeOkHttpClient(TimeOutConfig timeout) {
		if (timeout == null)
			return new OkHttpClient();
		else
			return new OkHttpClient().newBuilder().connectTimeout(timeout.getConnectTimeout(), timeout.getTimeunit())
					.writeTimeout(timeout.getWriteTimeout(), timeout.getTimeunit())
					.readTimeout(timeout.getReadTimeouts(), timeout.getTimeunit()).build();
	}

	public static OkHttpClient getUnsafeOkHttpClient(TimeOutConfig timeout) {
		OkHttpClient.Builder builder = null;
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[] {};
				}
			} };

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			if (timeout == null)
				builder = new OkHttpClient.Builder();
			else
				builder = new OkHttpClient().newBuilder()
						.connectTimeout(timeout.getConnectTimeout(), timeout.getTimeunit())
						.writeTimeout(timeout.getWriteTimeout(), timeout.getTimeunit())
						.readTimeout(timeout.getReadTimeouts(), timeout.getTimeunit());

			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true);

			return builder.build();

		} catch (final SSAPConnectionException e) {
			throw e;
		} catch (final Exception e) {
			throw new SSAPConnectionException("Error in getUnsafeOkHttpClient", e);
		}
	}

	public static String logId() {
		final String logId = MDC.get(CORRELATION_ID_LOG_VAR_NAME);
		if (null == logId)
			return new String("");
		else
			return logId;
	}
}
