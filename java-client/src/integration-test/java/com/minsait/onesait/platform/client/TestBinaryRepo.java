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
import com.minsait.onesait.platform.client.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.client.model.BinaryDataFile;
import com.minsait.onesait.platform.testing.IntegrationTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Category(IntegrationTest.class)
public class TestBinaryRepo {

	private final static String USERNAME = "developer";
	private final static String PASSWORD = "Changed!";
	private final static String SERVER = "https://development.onesaitplatform.com/controlpanel/";
	private final static String RESOURCES_FILE = "onesaitCloudPlatform_GettingStarted.pdf";

	private static BinaryRepositoryClient client = null;
	private static File myFile = null;

	@BeforeClass
	public static void startUp() {
		// Create binary repository RESTFull client
		try {
			// System.setProperty("java.net.useSystemProxies", "true");
			myFile = new File(BinaryRepoAppExample.class.getClassLoader().getResource(RESOURCES_FILE).getFile());
			client = new BinaryRepositoryClient(USERNAME, PASSWORD, SERVER);
		} catch (final BinaryRepositoryException e) {
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
			client.updateBinaryFile(newFileId, myFile, metadata);
			Assert.assertTrue("Updated binary file {}" + newFileId, true);

			// delete the binary file
			client.removeBinaryFile(newFileId);
			Assert.assertTrue("Deleted binary file {}" + newFileId, true);

		} catch (final Exception e) {
			Assert.fail(e.getMessage());
		}
	}

}
