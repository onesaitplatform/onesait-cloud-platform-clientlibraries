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
/*******************************************************************************
 * Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 * 
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.client.springboot.fromjson;

import java.util.ArrayList;
import java.util.List;

public class GeometryLinestring extends Geometry {

	private List<Double[]> coordinates;

	public GeometryLinestring() {
		super();
		this.coordinates = new ArrayList<Double[]>();
		this.type = GeometryType.LINE_STRING;
	}

	public List<Double[]> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<Double[]> coordinates) {
		this.coordinates = coordinates;
	}

	public GeometryType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "GeometryLinestring [coordinates=" + coordinates + ", type=" + type + "]";
	}

}
