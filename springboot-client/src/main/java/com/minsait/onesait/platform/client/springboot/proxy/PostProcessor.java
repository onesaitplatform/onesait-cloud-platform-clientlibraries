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
package com.minsait.onesait.platform.client.springboot.proxy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;
import com.minsait.onesait.platform.client.springboot.aspect.notifier.OPEntity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

	@Getter
	private ApplicationContext applicationContext;

	@Getter
	private Set<Class<?>> classEntities;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		classEntities = new HashSet<>();
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		log.info("Scanning for resources...");

		final Set<Class<?>> classSet = new HashSet<>();

		final List<String> packagesToScan = getComponentScanPackages();
		for (final String packages : packagesToScan) {
			try {
				final Reflections ref = new Reflections(packages);
				final Set<Class<?>> classes = ref.getTypesAnnotatedWith(IoTBrokerRepository.class);

				for (final Class<?> cl : classes) {
					classSet.add(cl);
				}

				final Set<Class<?>> entities = ref.getTypesAnnotatedWith(OPEntity.class);

				for (final Class<?> cl : entities) {
					classEntities.add(cl);
				}
			} catch (final Exception e) {
				log.warn("Could not load packages: {}, skipping", packages);
			}
		}

		for (final Class<?> cl : classSet) {
			registerClassByName(cl, beanFactory);
		}

	}

	private List<String> getComponentScanPackages() {

		final List<String> scanPackages = new ArrayList<String>();

		applicationContext.getBeansWithAnnotation(ComponentScan.class).forEach((name, instance) -> {

			final Set<ComponentScan> scans = AnnotatedElementUtils.findMergedRepeatableAnnotations(instance.getClass(),
					ComponentScan.class);

			for (final ComponentScan scan : scans) {
				scanPackages.addAll(Arrays.asList(scan.basePackages()));
			}

		});

		log.debug("Scanning annnotations in packages {}", Arrays.toString(scanPackages.toArray()));
		return scanPackages;

	}

	private void registerClassByName(Class<?> clazz, ConfigurableListableBeanFactory beanFactory) {
		try {
			final boolean isInterface = clazz.isInterface();
			final boolean isSofiaRepository = clazz.isAnnotationPresent(IoTBrokerRepository.class);

			if (isInterface && isSofiaRepository) {
				final ClassLoader classLoader = clazz.getClassLoader();
				final Class<?>[] classes = new java.lang.Class[] { clazz };
				final String annotationValue = clazz.getAnnotation(IoTBrokerRepository.class).value();

				Class<?> typeArgument = null;
				final Type[] typeGenericInterfaces = clazz.getGenericInterfaces();
				if (typeGenericInterfaces.length > 0) {
					final ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericInterfaces()[0];
					final Type[] typeArguments = parameterizedType.getActualTypeArguments();
					typeArgument = (Class<?>) typeArguments[0];
					log.info(typeArgument.getName());
				}

				final InvocationHandler invocationHandler = new InvocationHandler(annotationValue, applicationContext,
						typeArgument);
				final Object proxy = Proxy.newProxyInstance(classLoader, classes, invocationHandler);
				beanFactory.registerSingleton(clazz.getCanonicalName(), proxy);
				log.info("Registered proxy for {}", clazz.getName());

			}
		} catch (final NoClassDefFoundError e) {
			log.warn("Unable to get class for name: {}", clazz.getName());
		}
	}

}
