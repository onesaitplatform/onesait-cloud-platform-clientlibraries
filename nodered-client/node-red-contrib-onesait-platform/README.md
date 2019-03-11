# onesait-platform Node-RED Client

![onesait-platform](https://www.minsait.com/sites/default/files/minsait2018/logo-onesait-platform.png)

## Copyright notice

© 2013-18 Minsait

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## API documentation

Before using the SSAP API for the first time, we strongly recommend that you learn the main concepts of the Sofia2 platform. They have been included in the Sofia2 developer documentation, which can be downloaded from http://sofia2.com/desarrollador_en.html.

The Api source code comes with a test suite where you can see an usage example of every possible query and format

## Repository contents
This repository contains the following nodes to interact with onesait-platform from Node-RED:

* **onesait-platform-connection-config**: Configuration nodes are scoped globally by default, this means the state will be shared between flows. This node represent a shared connection to a remote system. In that instance, the config node is responsible for creating the connection (MQTT) and making it available to the nodes that use the config node. REST interface is also a possibility to connect to the platform. The following parameters are required to define the connection:
  * Protocol: MQTT or REST. For this connection is necessary to indicate the IP and Port number of the Endpoint. Our public available CloudLab instance would have this configuration: 
  ```
  REST: https://cloudlab.onesaitplatform.online
  MQTT: ping cloudlab.onesaitplatform.online and obtain IP. Port is 1883 as defined at IANA as MQTT over TCP
  ```
  * IoTClient and Instance: A valid IoTClient registered within the platform. To create one please go to: https://cloudlab.onesaitplatform.online/controlpanel/devices/list
  * Token: Identification number for the IoTClient in use.
  * Renovate session: Connection renewal time (used only in MQTT connection).

* **onesait-platform-delete**: This node deletes data from an ontology according to a query, or by RTDB ID. The following parameters are required:
  * Ontology: Name of the ontology. To create an ontology please go to: https://cloudlab.onesaitplatform.online/controlpanel/ontologies/list
  * Delete Type: Type may be by query-based or by Id.
  * Query/Id: Query or Id to delete, depending on the chosen type.
  * Query Type: SQL or NATIVE(MongoDB for CloudLab deployment)

* **onesait-platform-insert**: This node inserts data in an ontology. The following parameters are required:
  * Ontology: Name of the ontology. To create an ontology please go to: https://cloudlab.onesaitplatform.online/controlpanel/ontologies/list
  * Instance: A valid JSON instance for the selected ontology.

* **onesait-platform-leave**: This node close the actual session with the platform.

* **onesait-platform-query**: This node execute a query on an ontology. The following parameters are required:
  * Ontology: Name of the ontology. To create an ontology please go to: https://cloudlab.onesaitplatform.online/controlpanel/ontologies/list
  * Query: Query to execute.
  * Query Type: SQL or NATIVE(MongoDB for CloudLab deployment)

* **onesait-platform-update**: This node updates data in an ontology. The following parameters are required:
  * Ontology: Name of the ontology. To create an ontology please go to: https://cloudlab.onesaitplatform.online/controlpanel/ontologies/list
  * Update Type: Type may be by query-based or by Id.
  * Id: Instance Id to be updated.
  * Query: Query to update.
  * Query Type: SQL or NATIVE(MongoDB for CloudLab deployment)
* **onesait-platform-subscribe**: This node subscribes to any interaction with the selected ontology. The following parameters are required:
  * Ontology: Name of the ontology. To create an ontology please go to: https://cloudlab.onesaitplatform.online/controlpanel/ontologies/list
  * Query Type: SQL or NATIVE(MongoDB for CloudLab deployment)

## Example Flow
* **Demo flow**: An example flow is available and ready to test at https://flows.nodered.org/flow/989c8da7c08465f132882f24740c835f
  
## Contact information

If you need support from us, please feel free to contact us at [support@onesaitplatform.com](mailto:support@onesaitplatform.com).

And if you want to contribute, send us a pull request.
