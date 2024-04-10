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
package com.minsait.onesait.platform.config.client.encryptor;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasyptConfig implements ApplicationContextAware{

	@Value("${onesaitplatform.encryptor.passphrase:Bp0-3ncr1pt0r!}")
	private String password;

	@Value("${onesaitplatform.encryptor.algorithm:PBEWithMD5AndDES}")
	private String algorithm;

	@Value("${onesaitplatform.encryptor.providerName:SunJCE}")
	private String providerName;

	@Value("${onesaitplatform.encryptor.saltGeneratorClassName:org.jasypt.salt.RandomSaltGenerator}")
	private String saltGeneratorClassName;

	@Value("${onesaitplatform.encryptor.ivGeneratorClassName:org.jasypt.salt.NoOpIVGenerator}")
	private String ivGeneratorClassName;

	@Value("${onesaitplatform.encryptor.outputType:base64}")
	private String outputType;

	@Value("${onesaitplatform.encryptor.iterations:1000}")
	private String iterations;

	@Value("${onesaitplatform.encryptor.poolSize:1}")
	private String poolSize;

	@Bean("jasyptStringEncryptor")
	public StringEncryptor stringEncryptor() {
		final PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		final SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword(password);
		config.setAlgorithm(algorithm);
		config.setKeyObtentionIterations(iterations);
		config.setPoolSize(poolSize);
		config.setProviderName(providerName);
		config.setSaltGeneratorClassName(saltGeneratorClassName);
		config.setStringOutputType(outputType);
		encryptor.setConfig(config);
		return encryptor;
	}
	
	private static ApplicationContext context;

	public static StringEncryptor getEncryptor() {
		return (StringEncryptor) context.getBean("jasyptStringEncryptor");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		setContext(applicationContext);

	}

	private synchronized void setContext(ApplicationContext applicationContext) {
		context = applicationContext;
	}
}
