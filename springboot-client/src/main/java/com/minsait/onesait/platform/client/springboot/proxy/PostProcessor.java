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
package com.minsait.onesait.platform.client.springboot.proxy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

	private ApplicationContext applicationContext;

	private static final String CLASS_STR = ".class";

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.info("Scanning for resources...");
		String scanPath = "";
//		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();

		Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(scanPath);
		} catch (IOException e) {
			log.warn("Unable to load resources from path '{}'", scanPath, e);
			return;
		}
		while (resources.hasMoreElements()) {
			URL resourceUrl = resources.nextElement();
			String resourceName = resourceUrl.toString();
			log.info("Processing resource, name: {}, path: {}", resourceName, resourceUrl.getPath());
			boolean isLibrary = resourceName.contains("BOOT-INF/lib");
			boolean isJarFile = resourceName.startsWith("jar:file");
			if (!isLibrary && isJarFile) {
				processJarResource(resourceName, beanFactory);
			} else if (!isLibrary) {
				try {
					File directory = new File(new URI(resourceName).getPath());
					findClasses(directory, scanPath, applicationContext, beanFactory);
				} catch (ClassNotFoundException e) {
					log.warn("Class not found", e);
				} catch (Exception e) {
					log.warn("Exception captured: '{}'", resourceName, e);
				}
			}
		}
	}

	private void processJarResource(String resourceName, ConfigurableListableBeanFactory beanFactory) {
		log.debug("Processing Jar resource {}", resourceName);
		try {
			URL url = new URL(resourceName);
			JarURLConnection connection = (JarURLConnection) url.openConnection();
			JarFile jarFile = connection.getJarFile();
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				log.debug("Entry name: {}, D: {}, ", entry.getName(), entry.isDirectory());
				if (!entry.isDirectory() && entry.getName().endsWith(CLASS_STR)) {
					String className = entry.getName().replace("/", ".").replace(CLASS_STR, "");
					log.info("Registering bean class '{}' from scanned file: '{}'", className, entry.getName());
					registerClassByName(className, beanFactory);
				}
			}
		} catch (MalformedURLException e) {
			log.warn("Bad url: {}", resourceName);
		} catch (IOException e) {
			log.warn("IO exception", e);
		}
	}

	private void registerClassByName(String className, ConfigurableListableBeanFactory beanFactory) {
		try {
			Class clazz = Class.forName(className);
			boolean isInterface = clazz.isInterface();
			boolean isSofiaRepository = clazz.isAnnotationPresent(IoTBrokerRepository.class);

			if (isInterface && isSofiaRepository) {
				ClassLoader classLoader = clazz.getClassLoader();
				Class[] classes = new java.lang.Class[] { clazz };
				String annotationValue = ((IoTBrokerRepository) clazz.getAnnotation(IoTBrokerRepository.class)).value();

				Class<?> typeArgument = null;
				Type[] typeGenericInterfaces = clazz.getGenericInterfaces();
				if (typeGenericInterfaces.length > 0) {
					ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericInterfaces()[0];
					Type[] typeArguments = parameterizedType.getActualTypeArguments();
					typeArgument = (Class<?>) typeArguments[0];
					log.info(typeArgument.getName());
				}

				InvocationHandler invocationHandler = new InvocationHandler(annotationValue, applicationContext,
						typeArgument);
				Object proxy = Proxy.newProxyInstance(classLoader, classes, invocationHandler);
				beanFactory.registerSingleton(clazz.getCanonicalName(), proxy);
				log.info("Registered proxy for {}", className);

			}
		} catch (ClassNotFoundException e) {
			log.warn("Unable to get class for name: {}", className);
		}
	}

	/**
	 * Fetch interfaces annotated with @Sofia2Repository, creates proxies for them
	 * and registers beans.
	 * 
	 * @param directory
	 * @param packageName
	 * @param context
	 * @throws ClassNotFoundException
	 */
	private void findClasses(File directory, String packageName, ApplicationContext context,
			ConfigurableListableBeanFactory beanFactory) throws ClassNotFoundException {

		log.info("Looking for IoTBrokerClient annotations");

		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (packageName.equals("")) {
					findClasses(file, file.getName(), context, beanFactory);
				} else {
					findClasses(file, packageName + "." + file.getName(), context, beanFactory);
				}
			} else if (file.getName().endsWith(CLASS_STR)) {
				Class clazz;
				if (packageName.equals("")) {
					clazz = Class.forName(file.getName().substring(0, file.getName().length() - 6));
				} else {
					clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
				}
				if (clazz.isInterface() && clazz.getAnnotations().length > 0
						&& clazz.getAnnotations()[0] instanceof IoTBrokerRepository) {

					Class<?> typeArgument = null;
					Type[] typeGenericInterfaces = clazz.getGenericInterfaces();
					if (typeGenericInterfaces.length > 0) {
						ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericInterfaces()[0];
						Type[] typeArguments = parameterizedType.getActualTypeArguments();
						typeArgument = (Class<?>) typeArguments[0];
						log.info(typeArgument.getName());
					}

					Object object = Proxy.newProxyInstance(clazz.getClassLoader(), new java.lang.Class[] { clazz },
							new InvocationHandler(((IoTBrokerRepository) clazz.getAnnotations()[0]).value(),
									applicationContext, typeArgument));
					beanFactory.registerSingleton(clazz.getCanonicalName(), object);
					log.info("Created proxy bean for '{}'", clazz.getCanonicalName());
				}
			}
		}
	}

}
