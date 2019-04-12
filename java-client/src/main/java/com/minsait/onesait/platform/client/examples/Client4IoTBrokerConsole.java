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
package com.minsait.onesait.platform.client.examples;

import java.time.Month;
import java.util.List;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.system.SystemTextTerminal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client4IoTBrokerConsole {

	public Client4IoTBrokerConsole(String way) {

		if (way.equalsIgnoreCase("swing")) {
			textIO = TextIoFactory.getTextIO();
			terminal = textIO.getTextTerminal();

		} else {
			terminal = new SystemTextTerminal();
			textIO = new TextIO(terminal);

		}
	}

	private static Client4IoTBrokerConsole console = null;

	private TextIO textIO = null;
	private TextTerminal terminal = null;
	private String ontology = null;
	private String cpURL = null;
	private String iotbrokerURL = null;
	private String token = null;
	private String device = null;
	private RestClient client = null;

	public static void main(String[] args) {

		if (args != null && args.length > 0)
			console = new Client4IoTBrokerConsole(args[0]);
		else
			console = new Client4IoTBrokerConsole("text");
		console.start();
	}

	public void start() {
		terminal.println("________________________________________________________");
		terminal.println("");
		terminal.println("Command Line for Java Client of the onesait Cloud Platform:");
		terminal.println("________________________________________________________");
		ask4Info_Initial();

	}

	private void ask4Info_Initial() {
		terminal.println("***");
		terminal.println("STEP 1 ... Now we ask you for some information");
		terminal.println("***");

		cpURL = textIO.newStringInputReader()
				.withDefaultValue("https://s4citiespro.westeurope.cloudapp.azure.com/controlpanel/")
				.read("1. ControlPanel URL of the platform:");
		iotbrokerURL = textIO.newStringInputReader()
				.withDefaultValue("https://s4citiespro.westeurope.cloudapp.azure.com/iot-broker/")
				.read("2. IoTBroker URL of the platform:");

		Boolean demo = textIO.newBooleanInputReader().withDefaultValue(Boolean.TRUE).read(
				"3. Do you want to execute a simple example (REST, ontology created,...)? (if not we guide to create all needed)");

		Boolean hasUser = textIO.newBooleanInputReader().withDefaultValue(Boolean.TRUE)
				.read("4. Do you have an user with the role DEVELOPER in the ControlPanel " + cpURL);
		if (!hasUser)
			terminal.println("You can create freely an user on the ControlPanel, go to " + cpURL
					+ ", select 'New Account', fill information and select Developer");
		terminal.println("5. Please access the ControlPanel at " + cpURL + " with your user and password");

		if (demo)
			askInfo4_SimpleExample();
		else
			askInfo4_CompleteExample();
	}

	private void invoke(String iotbrokerURL, String ontology, String device, String token) {
		try {
			terminal.println("***");
			terminal.println("STEP 3 ... Invoking ");
			terminal.println("***");

			RestClient clientInvoke = new RestClient(iotbrokerURL);
			terminal.println("...");
			terminal.println("1. Attemping to connect to " + iotbrokerURL + " " + clientInvoke.getRestServer());
			clientInvoke.connect(token, device, device + ":1", true);
			terminal.println("2. Getting all existing instances of ontology " + ontology);
			terminal.println("3. Data: " + clientInvoke.getAll(ontology).toString().substring(0, 100));
			String query = "select * from " + ontology + " limit 5";
			terminal.println("4. Getting last 5 instances executing query '" + query + "'");
			final List<JsonNode> instancesFiltered = clientInvoke.query(ontology, query, SSAPQueryType.SQL);
			terminal.println("5. Data: " + instancesFiltered.toString());

		} catch (Exception e) {
			terminal.println("Error invoking:" + iotbrokerURL + " by:" + e.getMessage());
		}

	}

	public void askInfo4_SimpleExample() {
		terminal.println("***");
		terminal.println("STEP 2 ... ");
		terminal.println("***");
		terminal.println("1. When you are into the ControlPanel go to Menu ONTOLOGIES>My Ontologies");
		terminal.println("   Here you can see your own ontologies and other ontologies made PUBLIC by another users");
		terminal.println("   We use 'HelsinkiPopulation' as example");
		terminal.println(
				"   Go to Menu TOOLS>Query Tool, select HelsinkiPopulation and make a query, you can check the ontology has data.");
		terminal.println("2. Go to Menu DEVICES&SYSTEMS>My Device&Systems and pulse 'Create', fill information");
		terminal.println("   Select 'Client4HelsinkiPopulation_<youruser> as Identification");
		terminal.println("   Select 'HelsinkiPopulation' as ontology");
		terminal.println("   When completed, click New. You´ll be redirected to list.");
		ontology = textIO.newStringInputReader().withDefaultValue("HelsinkiPopulation")
				.read("3. Write the name of the ontology:");
		device = textIO.newStringInputReader().withDefaultValue("Client4HelsinkiPopulation_developer")
				.read("4. Write the Identification of the device you have created:");
		terminal.println(
				"On the table Select 'View It', you´ll see a Token like this '72acc1474c8d4e849d70fed1897414f3', point it");
		token = textIO.newStringInputReader().withMinLength(32)
				.read("5. Write the token you have seen on the ControlPanel:");
		terminal.println("...");
		invoke(cpURL, ontology, device, token);

	}

	public void askInfo4_CompleteExample() {
		terminal.print(
				"Now access the Control Panel if you have an user of create an user with rol Developer if not...");
	}

	public void doExample() {

		String user = textIO.newStringInputReader().withDefaultValue("admin").read("Username");

		String password = textIO.newStringInputReader().withMinLength(6).withInputMasking(true).read("Password");

		int age = textIO.newIntInputReader().withMinVal(13).read("Age");

		Month month = textIO.newEnumInputReader(Month.class).read("What month were you born in?");

		terminal.printf("\nUser %s is %d years old, was born in %s and has the password %s.\n", user, age, month,
				password);

	}

}
