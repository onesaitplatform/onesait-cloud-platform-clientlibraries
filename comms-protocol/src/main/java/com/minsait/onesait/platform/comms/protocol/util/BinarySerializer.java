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
package com.minsait.onesait.platform.comms.protocol.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.comms.protocol.binary.Base64;
import com.minsait.onesait.platform.comms.protocol.binary.BinarySizeException;
import com.minsait.onesait.platform.comms.protocol.binary.Encoder;
import com.minsait.onesait.platform.comms.protocol.binary.Encoding;
import com.minsait.onesait.platform.comms.protocol.binary.Mime;
import com.minsait.onesait.platform.comms.protocol.binary.Storage;

//@Slf4j
public class BinarySerializer {

	private static final String STORAGE_AREA = "storageArea";
	private static final String MEDIA = "media";

	// Method for Base64 encoding
	public JsonNode getJsonBinary(String fieldName, File file, Mime mime)
			throws FileNotFoundException, IOException, BinarySizeException {

		if (file.length() > 1000000)
			throw new BinarySizeException("File is too large, max bytes 1000000");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode returnNode = mapper.createObjectNode();
		JsonNode binaryNode = mapper.createObjectNode();
		JsonNode mediaNode = mapper.createObjectNode();
		Encoder base64 = new Base64();

		String data = base64.encode(IOUtils.toByteArray(new FileInputStream(file)));
		((ObjectNode) mediaNode).put("binaryEncoding", Encoding.BASE64.name());
		((ObjectNode) mediaNode).put("mime", mime.getValue());
		((ObjectNode) mediaNode).put("name", file.getName());
		((ObjectNode) mediaNode).put(STORAGE_AREA, Storage.SERIALIZED.name());

		((ObjectNode) binaryNode).put("data", data);
		((ObjectNode) binaryNode).set(MEDIA, mediaNode);

		((ObjectNode) returnNode).set(fieldName, binaryNode);

		return returnNode;
	}

	public byte[] binaryJsonToFile(JsonNode binaryNode) {
		Encoder base64 = new Base64();

		String data = binaryNode.get("data").asText();
		String binaryEnconding = binaryNode.get(MEDIA).get("binaryEnconding").asText();

		if (binaryEnconding.equals(Encoding.BASE64.name())) {
			return base64.decode(data);
		} else {
			return null;
		}

	}

	public void binaryJsonToFile(JsonNode binaryNode, String path) throws IOException {
		Encoder base64 = new Base64();

		String data = binaryNode.get("data").asText();
		String binaryEnconding = binaryNode.get(MEDIA).get("binaryEncoding").asText();
		String name = binaryNode.get(MEDIA).get("name").asText();

		if (binaryEnconding.equals(Encoding.BASE64.name()))

			if (!new File(path + "/" + name).isFile())
				FileUtils.writeByteArrayToFile(new File(path + "/" + name), base64.decode(data));
			else
				FileUtils.writeByteArrayToFile(
						new File(path + "/" + String.valueOf(Math.random() * 1000).replace(".", "") + name),
						base64.decode(data));

	}

}
