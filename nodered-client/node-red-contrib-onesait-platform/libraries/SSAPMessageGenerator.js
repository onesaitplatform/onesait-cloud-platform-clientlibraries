
function addQuotesToData(data){
	if (data.indexOf("{")!=0)
		data="{"+data+"}";

	return data;
}

var SSAPMessageGenerator = {

	/**
	 * JOIN By Token
	 */
	generateJoinByTokenMessage : function(token, device, instance) {
		var queryJoin = '{"messageId":"","sessionKey":null,"direction":"REQUEST","messageType":"JOIN","body":{"@type":"SSAPBodyJoinMessage","token":"'
		+ token + '","deviceTemplate":"'
		+ device + '","device":"'
		+ instance + '"}}';

		return queryJoin;
	},
	

	/**
	 * JOIN Renovate By Token
	 */
	generateJoinRenovateByTokenMessage : function(token, device, instance, sessionKey) {
		var queryJoin = '{"messageId":"","sessionKey":"'
		+ sessionKey + '","direction":"REQUEST","messageType":"JOIN","body":{"@type":"SSAPBodyJoinMessage","token":"'
		+ token + '","deviceTemplate":"'
		+ device + '","device":"'
		+ instance + '"}}';

		return queryJoin;
	},

	/**
	 * LEAVE Operation
	 */
	generateLeaveMessage : function(sessionKey) {
		var queryLeave = '{"messageId":"","sessionKey":"'
		+ sessionKey +'","direction":"REQUEST","messageType":"LEAVE","body":{"@type":"SSAPBodyJoinMessage","token":"null","deviceTemplate":"null","device":"null"}}';

		return queryLeave;
	},

	/**
	 * INSERT Message
	 */
	generateInsertMessage : function(data, ontology, sessionKey) {
		if(ontology===undefined || ontology==null){
			ontology='null'
		}else{
			ontology='"'+ontology+'"';
		}
		var queryInsert = '{"messageId": null,"sessionKey": "'
				+ sessionKey + '","direction":"REQUEST","messageType":"INSERT","body": {"@type":"SSAPBodyInsertMessage","ontology":'
				+ ontology + ',"data":'
				+ data + '}}';

		return queryInsert;
	},

	/**
	 * QUERY with queryType Operation
	 */
	generateQueryWithQueryTypeMessage : function(query, ontology, queryType, sessionKey) {
		if(ontology===undefined || ontology==null){
			ontology='null'
		}else{
			ontology='"'+ontology+'"';
		}
		var querySib='';

		var querySib = '{"messageId": null,"sessionKey": "'
			+ sessionKey	+'","direction": "REQUEST","messageType": "QUERY","body": {"@type":"SSAPBodyQueryMessage","ontology":'
			+ ontology +',"query":"'
			+ query +'","queryType":"'
			+ queryType +'","resultFormat":null}}';

		return querySib
	},

	/**
	 * REMOVE Operation
	 */
	/*generateDeleteMessage : function(query, ontology, sessionKey) {
		if(ontology===undefined || ontology==null){
			ontology='null'
		}
		var queryRemove = '{"messageId": null,"sessionKey":"'
		  + sessionKey + '","direction":"REQUEST","messageType": "DELETE","body": {"@type": "SSAPBodyDeleteMessage","ontology":"'
		  + ontology + '","query":"db.'
			+ ontology + '.remove('
			+ query + ')"}}';

		return queryRemove;
	},*/
	
	generateDeleteMessage : function(query, ontology, sessionKey) {
		if(ontology===undefined || ontology==null){
			ontology='null'
		}else{
			ontology='"'+ontology+'"';
		}

		var queryRemove = '{"messageId": null,"sessionKey":"'
			+ sessionKey + '","direction": "REQUEST","messageType": "DELETE","body": {"@type": "SSAPBodyDeleteMessage","ontology": '
			+ ontology + ',"query": "'
			+ query + '"}}';

		return queryRemove;
	},

	/**
	 * REMOVE_BY_ID Operation
	 */
	generateDeleteByIdMessage : function(id, ontology, sessionKey) {
		if(ontology===undefined || ontology==null){
			ontology='null'
		}else{
			ontology='"'+ontology+'"';
		}
		var queryRemove = '{"messageId": null,"sessionKey":"'
		  + sessionKey + '","direction":"REQUEST","messageType": "DELETE_BY_ID","body": {"@type": "SSAPBodyDeleteByIdMessage","ontology":'
		  + ontology + ',"id": "'
			+ id + '"}}';

		return queryRemove;
	},

	/**
	 * UPDATE Operation
	 */
	generateUpdateMessage : function(query, ontology, sessionKey) {
		if(ontology===undefined || ontology==null){
			ontology='null'
		}else{
			ontology='"'+ontology+'"';
		}

		var queryUpdate = '{"messageId": null,"sessionKey":"'
			+ sessionKey + '","direction": "REQUEST","messageType": "UPDATE","body": {"@type": "SSAPBodyUpdateMessage","ontology": '
			+ ontology + ',"query": "'
			+ query + '"}}';

		return queryUpdate;
	},

	/**
	 * UPDATE_BY_ID Operation
	 */
	generateUpdateByIdMessage : function(id, data, ontology, sessionKey) {
		if(ontology===undefined || ontology==null){
			ontology='null'
		}else{
			ontology='"'+ontology+'"';
		}

		var queryUpdate = '{"messageId": null,"sessionKey":"'
			+ sessionKey + '","direction": "REQUEST","messageType": "UPDATE_BY_ID","body": {"@type": "SSAPBodyUpdateByIdMessage","ontology": '
			+ ontology + ', "id":"'
			+ id + '","data":'
			+ data + '}}';

		return queryUpdate;
	},


	/**
	 * SUBSCRIBE with queryType Operation
	 */
	generateSubscribeWithQueryTypeMessage : function(query, ontology, queryType, sessionKey) {
		if(ontology===undefined || ontology==null){
			ontology='null'
		}else{
			ontology='"'+ontology+'"';
		}
		var querySib='';

		var querySib = '{"messageId": null,"sessionKey": "'
			+ sessionKey +'","direction": "REQUEST","messageType": "SUBSCRIBE","body":{"@type":"SSAPBodySubscribeMessage","ontology":'
			+ ontology +',"query":"'
			+ query +'","queryType":"'
			+ queryType +'"}}';

		return querySib
	},


	/**
	 * UNSUBSCRIBE Operation
	 */
	generateUnsubscribeMessage : function(subscriptionId, sessionKey) {
		var querySib='';

		var querySib = '{"messageId": null,"sessionKey": "'
			+ sessionKey +'","direction": "REQUEST","messageType": "UNSUBSCRIBE","body":{"@type":"SSAPBodyUnsubscribeMessage","subscriptionId":"'
			+ subscriptionId +'"}}';

		return querySib
	}

}

exports.generateJoinByTokenMessage = SSAPMessageGenerator.generateJoinByTokenMessage;
exports.generateJoinRenovateByTokenMessage = SSAPMessageGenerator.generateJoinRenovateByTokenMessage;
exports.generateLeaveMessage = SSAPMessageGenerator.generateLeaveMessage;
exports.generateInsertMessage = SSAPMessageGenerator.generateInsertMessage;
exports.generateQueryWithQueryTypeMessage = SSAPMessageGenerator.generateQueryWithQueryTypeMessage;
exports.generateDeleteMessage = SSAPMessageGenerator.generateDeleteMessage;
exports.generateDeleteByIdMessage = SSAPMessageGenerator.generateDeleteByIdMessage;
exports.generateUpdateMessage = SSAPMessageGenerator.generateUpdateMessage;
exports.generateUpdateByIdMessage = SSAPMessageGenerator.generateUpdateByIdMessage;
exports.generateSubscribeWithQueryTypeMessage = SSAPMessageGenerator.generateSubscribeWithQueryTypeMessage;
exports.generateUnsubscribeMessage = SSAPMessageGenerator.generateUnsubscribeMessage;
