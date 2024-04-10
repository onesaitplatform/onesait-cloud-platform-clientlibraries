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
package com.minsait.onesait.platform.client;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.minsait.onesait.platform.client.enums.RepositoryType;
import com.minsait.onesait.platform.client.examples.BinaryRepoAppExample;
import com.minsait.onesait.platform.client.model.BinaryDataFile;
import com.minsait.onesait.platform.testing.IntegrationTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Category(IntegrationTest.class)
public class TestBinaryRepo {

	private static final String SERVER = "https://lab.onesaitplatform.com/controlpanel/";
	private static final String RESOURCES_FILE = "Arbol.jpeg";
	private static final String TOKEN = "Bearer AT-863643-t-uCSYyrKtabDzEI3W7lduzFwfxaJQfT";

	private static BinaryRepositoryClient client = null;
	private static File myFile = null;

	@BeforeClass
	public static void startUp() {
		// Create binary repository RESTFull client
		try {
			// System.setProperty("java.net.useSystemProxies", "true");
			myFile = new File(BinaryRepoAppExample.class.getClassLoader().getResource(RESOURCES_FILE).getFile());

			// KEYCLOAK AUTH
//			client = new BinaryRepositoryClient("meteringAdmin", "Minsait2019!", "https://ouidemo.cwbyminsait.com",
//					AuthServerType.KEYCLOAK, "oui_new");
			// CONTROLPANEL AUTH"
			client = new BinaryRepositoryClient("developer", "Changed2019!", SERVER, "onesaitplatform");

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	// Run once, e.g close connection, cleanup
	@AfterClass
	public static void closeUp() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void close() {
	}

	@Test
	public void test_CRUDFile() {
		try {

			final String newFileId = client.addBinaryFile(myFile, "", RepositoryType.MONGO_GRIDFS);
			log.info("New file ID is {}", newFileId);
			Assert.assertTrue(newFileId != null);

			// Retrieve binary file from platform
			final BinaryDataFile bfile = client.getBinaryFile(newFileId);
			log.info("Retrieved file with name \"{}\"", bfile.getFileName());
			Assert.assertTrue(bfile.getFileName().equals(RESOURCES_FILE));

			// Update binary file
			final String metadata = "{\"private\" : true}";
			// client.updateBinaryFile(newFileId, myFile, metadata);
			// Assert.assertTrue("Updated binary file {}" + newFileId, true);

			// delete the binary file
			client.removeBinaryFile(newFileId);
			Assert.assertTrue("Deleted binary file {}" + newFileId, true);

		} catch (final Exception e) {
			Assert.fail(e.getMessage());
		}
	}

}
