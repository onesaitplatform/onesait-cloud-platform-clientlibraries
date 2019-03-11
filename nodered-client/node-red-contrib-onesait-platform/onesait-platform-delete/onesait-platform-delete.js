module.exports = function(RED) {
	var ssapMessageGenerator = require('../libraries/SSAPMessageGenerator');
	var sofiaConfig = require('../onesait-platform-connection-config/onesait-platform-connection-config');
	var ssapResourceGenerator = require('../libraries/SSAPResourceGenerator');

	var http = null;
	var isHttps = false;

  function Delete(n) {
    RED.nodes.createNode(this,n);

		var node = this;
		this.deleteType = n.deleteType;
		this.ontology = n.ontology;
		this.query = n.query;
		this.queryType = n.queryType;

		// Retrieve the server (config) node
		var server = RED.nodes.getNode(n.server);

		this.on('input', function(msg) {
			var deleteType="";
			var ontology="";
			var queryType="";
			var query="";
			if(this.ontology=="") {ontology = msg.ontology;}
				else {ontology=this.ontology;}
			if(this.queryType=="") {queryType = msg.queryType;}
				else {queryType=this.queryType;}
			if(this.query=="") {
			   query = msg.query;
				 deleteType = msg.deleteType;
			}	else {
			   query=this.query;
				 deleteType=this.deleteType;
			}

			if (server) {
				var protocol = server.protocol;
				if(protocol.toUpperCase() == "MQTT".toUpperCase()){

					if (server.sessionKey==null || server.sessionKey=="") {
						node.warn("No active sessionKey stored. Sending JOIN message...");
						var join = ssapMessageGenerator.generateJoinByTokenMessage(server.token, server.device, server.instance);
						var joinState = server.sendToSib(join);
						joinState.then(function(joinResponse){
							if (joinResponse.sessionKey !== null) {
								server.sessionKey = joinResponse.sessionKey;
								node.log("Using sessionKey:"+server.sessionKey);
								if (deleteType === "DELETE"){
									var queryDelete = ssapMessageGenerator.generateDeleteMessage(query, ontology, server.sessionKey);
								}else if(deleteType === "DELETE_BY_ID"){
									var queryDelete = ssapMessageGenerator.generateDeleteByIdMessage(query, ontology, server.sessionKey);
								}else{
									node.error("Error: Delete message type not supported");
								}
								var state = server.sendToSib(queryDelete);
								
								state.then(function(response){
									var body = response.body;
									if(body.ok){
										msg.payload=body.data;
										node.send(msg);
										node.log(JSON.stringify(msg.payload));
									}else{
										node.error("Error sending the delete SSAP message by:"+body.error);
										msg.payload=body.error;
										if(body.errorCode == "AUTHORIZATION"){
											node.warn("Error: The sessionKey is not valid. Generating new Session....");
											server.generateSession();
										}
										node.send(msg);
									}
								});


							} else {//Sobre todo renovación de sesión
								//check exception management etc.
								node.error("No sessionKey retrieved");
							}
						});
					}
					else{
						node.log("Using SessionKey:"+server.sessionKey);
						
						if (deleteType === "DELETE"){
							var queryDelete = ssapMessageGenerator.generateDeleteMessage(query, ontology, server.sessionKey);
						}else if(deleteType === "DELETE_BY_ID"){
							var queryDelete = ssapMessageGenerator.generateDeleteByIdMessage(query, ontology, server.sessionKey);
						}else{
							node.error("Error: Delete message type not supported");
						}
						var state = server.sendToSib(queryDelete);

						state.then(function(response){
							var body = response.body;
							if(body.ok){
								msg.payload=body.data;
								node.send(msg);
								node.log(JSON.stringify(msg.payload));
							}else{
								node.error("Error sending the update SSAP message by:"+body.error);
								msg.payload=body.error;
								if(body.errorCode == "AUTHORIZATION"){
										node.warn("Error: The sessionKey is not valid. Generating new Session....");
										var join = ssapMessageGenerator.generateJoinByTokenMessage(server.token, server.device, server.instance);
										var joinState = server.sendToSib(join);
									
										joinState.then(function(joinResponse){
											if (joinResponse.sessionKey !== null) {
												server.sessionKey = joinResponse.sessionKey;
												node.log("Using sessionKey:"+server.sessionKey);
												if (deleteType === "DELETE"){
													var queryDelete = ssapMessageGenerator.generateDeleteMessage(query, ontology, server.sessionKey);
												}else if(deleteType === "DELETE_BY_ID"){
													var queryDelete = ssapMessageGenerator.generateDeleteByIdMessage(query, ontology, server.sessionKey);
												}else{
													node.error("Error: Delete message type not supported");
												}
												var state = server.sendToSib(queryDelete);


												state.then(function(response){
													var body = response.body;
													if(body.ok){
														msg.payload=body.data;
														node.send(msg);
														node.log(JSON.stringify(msg.payload));
													}else{
														node.error("Error sending the update SSAP message by:"+body.error);
														msg.payload=body.error;
														if(body.errorCode == "AUTHORIZATION"){
															node.warn("Error: The sessionKey is not valid. Generating new Session....");
															server.generateSession();
														}
														node.send(msg);
													}
												});
											} else {//Sobre todo renovación de sesión
												//check exception management etc.
												node.error("No sessionKey retrieved");
											}
										});
								}
								node.send(msg);
							}
						});
					}
				}
				else if(protocol.toUpperCase() == "REST".toUpperCase()){
					var updateBody = query;
					var query = encodeURIComponent(updateBody);

					var endpoint = server.endpoint;
					var arr = endpoint.toString().split(":");
					var clientPlatform = server.device;
					var instance = server.instance;
					var token = server.token;
					var hostProtocol = arr[0];
					var host;
					var port = 8081;
					// console.log("[ REST ] Endpoint:"+endpoint);
					// console.log("[ REST ] device:"+clientPlatform);
					// console.log("[ REST ] Instance:"+instance);
					// console.log("[ REST ] token:"+token);
					// console.log("[ REST ] Using "+arr[0]);

					if (arr[0].toUpperCase()=='HTTPS'.toUpperCase()) {
						isHttps=true;
						//console.log("Using HTTPS:"+arr[0]);
					}
					if(arr[0].toUpperCase()=="HTTP".toUpperCase()||arr[0].toUpperCase()=='HTTPS'.toUpperCase()){
						host=arr[1].substring(2, arr[1].length);
						if(arr.length>2){
							port = parseInt(arr[arr.length-1]);
						}
					}else{
						host = arr[0];
						if(arr.length>1){
							port = parseInt(arr[arr.length-1]);
						}
					}

					var baseUrl = hostProtocol + "://" + host;// + ":" + port;
					if (deleteType === "DELETE"){
						var pathRemove = ssapResourceGenerator.generateDeleteResource(baseUrl, ontology, query);
						var methodRemove = "GET";
					}else if(deleteType === "DELETE_BY_ID"){ // The id to be removed is on the query variable.
						var pathRemove = ssapResourceGenerator.generateDeleteByIdResource(baseUrl, ontology, query);
						var methodRemove = "DELETE";
					}else{
						node.error("Error: Delete message type not supported");
					}

					var headersDelete = {
						'Content-Type' : 'application/json',
						'Accept' : 'application/json',
						'Authorization' : server.sessionKey
					};
					var optionsDelete = {
					  host: host,
					  path: pathRemove,
					  method: methodRemove,
					  headers: headersDelete,
					  rejectUnauthorized: false
					};

					// do the GET POST call
					var resultDelete='';
					if (isHttps) {http= require('https');}
					else {http = require('http');}

					var reqDelete = http.request(optionsDelete, function(res) {
						//console.log(""+pathRemove);
						//console.log("Status code of the Delete call: ", res.statusCode);
						res.on('data', function(d) {
							resultDelete +=d;
						});
						res.on('end', function() {
							if(res.statusCode==400 || res.statusCode==401){
								//The sessionKey is not valid,
								//Trying to regenerate the sessionKey
								node.warn("No active sessionKey stored. Sending JOIN message...");
								var pathJoin = ssapResourceGenerator.generateJoinByTokenResource(baseUrl, token, clientPlatform, instance);

								var postheadersJoin = {
									'Accept' : 'application/json'
								};
								var optionsJoin = {
								  host: host,
								  path: pathJoin,
								  method: 'GET',
								  headers: postheadersJoin,
								  rejectUnauthorized: false
								};

								// do the JOIN POST call
								var resultJoin='';
								var reqPost = http.request(optionsJoin, function(res) {
									//console.log("Status code of the Join call: ", res.statusCode);
									res.on('data', function(d) {
										resultJoin +=d;
									});
									res.on('end', function() {
										resultJoin = JSON.parse(resultJoin);
										server.sessionKey=resultJoin.sessionKey;
										node.log("Using SessionKey:"+server.sessionKey);

										if (deleteType === "DELETE"){
											var pathRemove = ssapResourceGenerator.generateDeleteResource(baseUrl, ontology, query);
											var methodRemove = "GET";
										}else if(deleteType === "DELETE_BY_ID"){ // The id to be removed is on the query variable.
											var pathRemove = ssapResourceGenerator.generateDeleteByIdResource(baseUrl, ontology, query);
											var methodRemove = "DELETE";
										}else{
											node.error("Error: Delete message type not supported");
										}

										var headersDelete = {
											'Content-Type' : 'application/json',
											'Accept' : 'application/json',
											'Authorization' : server.sessionKey
										};
										var optionsDelete = {
										  host: host,
										  path: pathRemove,
										  method: methodRemove,
										  headers: headersDelete,
										  rejectUnauthorized: false
										};

										// do the GET POST call
										
										var resultDelete='';
										if (isHttps) {http= require('https');}
										else {http = require('http');}

										var reqDelete = http.request(optionsDelete, function(res) {
											//console.log(""+pathRemove);
											//console.log("Status code of the Delete call: ", res.statusCode);
											res.on('data', function(d) {
												resultDelete +=d;
											});
											res.on('end', function() {
												if(res.statusCode=="200"){
													try{
														node.log(resultDelete);
														resultDelete = JSON.parse(resultDelete);
														msg.payload=resultDelete;
													} catch (err) {
														msg.payload=resultDelete;
														node.error(resultDelete);
													}
													node.send(msg);
												}
											});

										});
										reqDelete.end();
										reqDelete.on('error', function(err) {
											console.log("Error:"+err);
											node.error("Error:"+err);
										});

									});

								});
								reqPost.write(pathJoin);
								reqPost.end();
								reqPost.on('error', function(err) {
									console.log("There was an error inserting the data: ", err);
								});

							} else if(res.statusCode==200){
								try{
									node.log(resultDelete);
									resultDelete = JSON.parse(resultDelete);
									msg.payload=resultDelete;
								} catch (err) {
									msg.payload=err;
								}
								node.send(msg);
							}
						});

					});
					reqDelete.end();
					reqDelete.on('error', function(err) {
						console.log("Error:"+err);
						node.error("Error:"+err);
					});
				}

			} else {
				console.log("Error");
			}
    });

  }
  RED.nodes.registerType("onesait-platform-delete",Delete);
}
