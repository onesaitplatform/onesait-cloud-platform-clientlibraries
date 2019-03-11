/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2016 - 2017  SPAIN
 *
 * All rights reserved
 ******************************************************************************/
function addQuotesToData(data){
	if (data.indexOf("{")!=0)
		data="{"+data+"}";

	return data;
}

function escapeJSONObject(datos){
	return datos.replace(/\"/g, "\\\"").replace(/\\\\\"/g, "\\\\\\\"");
}

var SSAPResourceGenerator = {


	/**
	 * JOIN By Token
	 */
	generateJoinByTokenResource : function(baseUrl, token, clientPlatform, instance) {

		var pathJoin = baseUrl + "/iot-broker/rest/client/join?token="
			+ token + "&clientPlatform="
			+ clientPlatform + "&clientPlatformId="
			+ instance;

		return pathJoin;
	},

	/**
	 * QUERY Operation
	 */
	generateQueryResource : function(baseUrl, ontology, query, queryType) {

		var pathQuery = baseUrl + "/iot-broker/rest/ontology/"
			+ ontology + "?query="
			+ query + "&queryType="
			+ queryType;

		return pathQuery;
	},

	/**
	 * LEAVE Operation
	 */
	generateLeaveResource : function(baseUrl, sessionKey) {
		var pathLeave = baseUrl + "/iot-broker/rest/client/leave";

		return pathLeave;
	},

	/**
	 * INSERT Message
	 */
	generateInsertResource : function(baseUrl, ontology) {
		var queryInsert = baseUrl + "/iot-broker/rest/ontology/"
			+ ontology;

		return queryInsert;
	},

	/**
	 * UPDATE Operation
	 */
	generateUpdateResource : function(baseUrl, ontology, query) {
		var pathUpdate = baseUrl + "/iot-broker/rest/ontology/"
			+ ontology + "/update?query="
			+ query;

		return pathUpdate;
	},

	/**
	 * UPDATE_BY_ID Operation
	 */
	generateUpdateByIdResource : function(baseUrl, ontology, id) {
		var pathUpdate = baseUrl + "/iot-broker/rest/ontology/"
			+ ontology + "/"
			+ id;

		return pathUpdate;
	},

	/**
	 * REMOVE Operation
	 */
	generateDeleteResource : function(baseUrl, ontology, query) {
		var pathRemove = baseUrl + "/iot-broker/rest/ontology/"
			+ ontology + "/delete?query="
			+ query;

		return pathRemove;
	},

	/**
	 * REMOVE_BY_ID Operation
	 */
	generateDeleteByIdResource : function(baseUrl, ontology, id) {
		var pathRemove = baseUrl + "/iot-broker/rest/ontology/"
			+ ontology + "/"
			+ id;

		return pathRemove;
	}

}

exports.generateJoinByTokenResource = SSAPResourceGenerator.generateJoinByTokenResource;
exports.generateQueryResource = SSAPResourceGenerator.generateQueryResource;
exports.generateLeaveResource = SSAPResourceGenerator.generateLeaveResource;
exports.generateInsertResource = SSAPResourceGenerator.generateInsertResource;
exports.generateUpdateResource = SSAPResourceGenerator.generateUpdateResource;
exports.generateUpdateByIdResource = SSAPResourceGenerator.generateUpdateByIdResource;
exports.generateDeleteResource = SSAPResourceGenerator.generateDeleteResource;
exports.generateDeleteByIdResource = SSAPResourceGenerator.generateDeleteByIdResource;
