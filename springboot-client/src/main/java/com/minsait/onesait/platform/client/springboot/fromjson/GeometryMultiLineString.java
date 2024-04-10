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
package com.minsait.onesait.platform.client.springboot.fromjson;

import java.util.ArrayList;
import java.util.List;

public class GeometryMultiLineString extends Geometry {

	private List<List<Double[]>> coordinates;

	public GeometryMultiLineString() {
		super();
		this.coordinates = new ArrayList<List<Double[]>>();
		this.type = GeometryType.MULTILINE_STRING;
	}

	public List<List<Double[]>> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<List<Double[]>> coordinates) {
		this.coordinates = coordinates;
	}

	public GeometryType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "GeometryPoligon [coordinates=" + coordinates + ", type=" + type + "]";
	}

}
