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
package com.minsait.onesait.platform.client.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class MQTTSecureConfiguration {

	private String keyStorePath;
	private String password;

	private final static String DEFAULT_PASSWORD = "changeIt!";
	private final static String RESOURCES_FILE = "clientdevelkeystore.jks";

	public MQTTSecureConfiguration(String keyStorePath, String password) {
		if (keyStorePath == null || password == null)
			useDefaultConfiguration();
		else {
			this.keyStorePath = keyStorePath;
			this.password = password;

		}
	}

	public SSLSocketFactory configureSSLSocketFactory() throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

		final KeyStore ks = KeyStore.getInstance("JKS");
		final InputStream jksInputStream = new FileInputStream(keyStorePath);
		ks.load(jksInputStream, password.toCharArray());

		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());

		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);

		final SSLContext sc = SSLContext.getInstance("TLS");
		final TrustManager[] trustManagers = tmf.getTrustManagers();
		sc.init(kmf.getKeyManagers(), trustManagers, null);

		final SSLSocketFactory ssf = sc.getSocketFactory();
		return ssf;
	}

	public void useDefaultConfiguration() {
		keyStorePath = getClass().getClassLoader().getResource(RESOURCES_FILE).getFile();
		password = DEFAULT_PASSWORD;
	}

}
