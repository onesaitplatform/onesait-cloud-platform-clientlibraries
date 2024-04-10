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
package com.minsait.onesait.platform.client.examples;

import java.io.File;
import java.io.IOException;

import com.minsait.onesait.platform.client.BinaryRepositoryClient;
import com.minsait.onesait.platform.client.enums.RepositoryType;
import com.minsait.onesait.platform.client.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.client.model.BinaryDataFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinaryRepoAppExample {

	private final static String USERNAME = "developer";
	private final static String PASSWORD = "Changed2019!";
	private final static String SERVER = "https://localhost:18000/controlpanel/";
	private final static String RESOURCES_FILE = "onesaitCloudPlatform_GettingStarted.pdf";

	public static void main(String[] args) throws BinaryRepositoryException, IOException {

		System.setProperty("java.net.useSystemProxies", "true");

		final File myFile = new File(BinaryRepoAppExample.class.getClassLoader().getResource(RESOURCES_FILE).getFile());

		// Create binary repository RESTFull client
		final BinaryRepositoryClient client = new BinaryRepositoryClient(USERNAME, PASSWORD, SERVER, "onesaitplatform");

		// Add binary file to platform
		final String newFileId = client.addBinaryFile(myFile, "", RepositoryType.MONGO_GRIDFS);
		log.info("New file ID is {}", newFileId);

		// Retrieve binary file from platform
		final BinaryDataFile bfile = client.getBinaryFile(newFileId);
		log.info("Retrieved file with name \"{}\"", bfile.getFileName());

		// Update binary file
		final String metadata = "{\"private\" : true}";
		client.updateBinaryFile(newFileId, myFile, metadata);
		log.info("Updated binary file {}", newFileId);

		// delete the binary file
		client.removeBinaryFile(newFileId);
		log.info("Deleted binary file {}", newFileId);

	}
}
