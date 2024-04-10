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
package com.minsait.onesait.platform.client.interfaces;

import com.minsait.onesait.platform.client.model.Notification;

public interface Notifier {
	public <T> String createOntologyFromPOJO(Class<T> clazz);

	public <T> String createOrUpdateOntology(Class<T> clazz);

	public String createOrUpdateOntology(String ontology, String schema);

	public void validateSchema(String ontology, String input);

	public void validateSchema(Object input);

	public boolean notify(Notification notification);

	public void notifyAsync(Notification notification);

}
