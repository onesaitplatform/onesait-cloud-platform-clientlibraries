/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
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
package com.minsait.onesait.platform.client.springboot.proxy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Clase inicializadora de SpringBoot se encarga de la carga de clases anotadas
 * como Sofia2Repository para su intrumentalizacion antes de tener disponible el
 * contexto de Spring *
 */
@Slf4j
public class IoTClient4SpringBootInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext arg0) {
		try {
			init(arg0);
		} catch (Exception e) {
			log.error("Error initialize:", e);
		}
	}

	public void init(ConfigurableApplicationContext context)
			throws ClassNotFoundException, IOException, URISyntaxException {
		String packageName = "";
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			try {
				URL resource = resources.nextElement();
				URI uri = new URI(resource.toString());
				dirs.add(new File(uri.getPath()));
			} catch (Exception e) {
				log.error("Error while loading resources:", e);
			}
		}
		for (File directory : dirs) {
			findClasses(directory, packageName, context);
		}
	}

	/**
	 * Metodo que busca todas las interfaces anotadas como Sofia2Repository y crea
	 * un Proxy para crear la logica de invocacion a Sofia2
	 * 
	 * @param directory
	 * @param packageName
	 * @param context
	 * @throws ClassNotFoundException
	 */
	private void findClasses(File directory, String packageName, ConfigurableApplicationContext context)
			throws ClassNotFoundException {
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (packageName.equals("")) {
					findClasses(file, file.getName(), context);
				} else {
					findClasses(file, packageName + "." + file.getName(), context);
				}
			} else if (file.getName().endsWith(".class")) {
				Class clase;
				if (packageName.equals("")) {
					clase = Class.forName(file.getName().substring(0, file.getName().length() - 6));
				} else {
					clase = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
				}
				if (clase.isInterface() && clase.getAnnotations().length > 0
						&& clase.getAnnotations()[0] instanceof IoTBrokerRepository) {
					Object objeto = Proxy.newProxyInstance(clase.getClassLoader(), new java.lang.Class[] { clase },
							new InvocationHandler(((IoTBrokerRepository) clase.getAnnotations()[0]).value(), context));
					ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) context;
					ConfigurableListableBeanFactory bf = ctx.getBeanFactory();
					bf.registerSingleton(clase.getCanonicalName(), objeto);
				}
			}
		}
	}

}
