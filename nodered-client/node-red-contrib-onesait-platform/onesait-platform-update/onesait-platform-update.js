module.exports = function(RED) {
	var ssapMessageGenerator = require('../libraries/SSAPMessageGenerator');
	var sofiaConfig = require('../onesait-platform-connection-config/onesait-platform-connection-config');
	var ssapResourceGenerator = require('../libraries/SSAPResourceGenerator');
	var http = null;
	var isHttps = false;

  function Update(n) {
    RED.nodes.createNode(this,n);

		var node = this;
		this.updateType = n.updateType;
		this.ontology = n.ontology;
		this.query = n.query;
		this.queryType = n.queryType;
		this.id = n.messageId;

		// Retrieve the server (config) node
		var server = RED.nodes.getNode(n.server);

		this.on('input', function(msg) {
			var ontology="";
			var queryType="";
			var query="";
			var id="";
			var updateType="";
			if(this.ontology=="") {ontology = msg.ontology;}
				else {ontology=this.ontology;}
			if(this.queryType=="") {queryType = msg.queryType;}
				else {queryType=this.queryType;}
			if(this.query=="") {
				updateType = msg.updateType;
				if(updateType === "UPDATE"){
					query = msg.query;
				}
				else if(updateType === "UPDATE_BY_ID"){
					query = JSON.stringify(msg.query);
				}
				id = msg.id;
			}else{
				updateType = this.updateType;
				if(updateType === "UPDATE"){
					query = this.query;
				}
				else if(updateType === "UPDATE_BY_ID"){
					query = this.query;
				}
				id = this.id;
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
								if (updateType === "UPDATE"){
									var pathUpdate = ssapMessageGenerator.generateUpdateMessage(query, ontology, server.sessionKey);
								}else if(updateType === "UPDATE_BY_ID"){
									var pathUpdate = ssapMessageGenerator.generateUpdateByIdMessage(id, query, ontology, server.sessionKey);
								}else{
									node.error("Error, update message type not supported");
								}
								var state = server.sendToSib(pathUpdate);
								
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
					else{
						node.log("Using SessionKey:"+server.sessionKey);
						
						if (updateType === "UPDATE"){
							var pathUpdate = ssapMessageGenerator.generateUpdateMessage(query, ontology, server.sessionKey);
						}else if(updateType === "UPDATE_BY_ID"){
							var pathUpdate = ssapMessageGenerator.generateUpdateByIdMessage(id, query, ontology, server.sessionKey);
						}else{
							node.error("Error, update message type not supported");
						}
						//console.log("Using query:"+pathUpdate);

						var state = server.sendToSib(pathUpdate);

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
												if (updateType === "UPDATE"){
													var pathUpdate = ssapMessageGenerator.generateUpdateMessage(query, ontology, server.sessionKey);
												}else if(updateType === "UPDATE_BY_ID"){
													var pathUpdate = ssapMessageGenerator.generateUpdateByIdMessage(id, query, ontology, server.sessionKey);
												}else{
													node.error("Error, update message type not supported");
												}
												var state = server.sendToSib(pathUpdate);


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
					// Enconding update query
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

					// Check https/http
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

					var baseUrl = hostProtocol + "://" + host;
					if (updateType === "UPDATE"){
						var pathUpdate = ssapResourceGenerator.generateUpdateResource(baseUrl, ontology, query);
						var methodUpdate = "GET";
						var headersUpdate = {
							'Content-Type' : 'application/json',
							'Accept' : 'application/json',
							'Authorization' : server.sessionKey
						};
					}else if(updateType === "UPDATE_BY_ID"){ // The id to be removed is on the query variable.
						var pathUpdate = ssapResourceGenerator.generateUpdateByIdResource(baseUrl, ontology, id);
						var methodUpdate = "PUT";
						var headersUpdate = {
							'Content-Type' : 'application/json',
							'Accept' : 'application/json',
							'Authorization' : server.sessionKey,
							'Content-Length' : Buffer.byteLength(updateBody, 'utf8')
						};
					}else{
						node.error("Error: Update message type not supported");
					}

					var optionsUpdate = {
					  host: host,
					  path: pathUpdate,
					  method: methodUpdate,
					  headers: headersUpdate,
					  rejectUnauthorized: false
					};
					// do the UPDATE PUT call
					var resultUpdate='';
					if (isHttps) {http= require('https');}
					else {http = require('http');}

					var reqUpdate = http.request(optionsUpdate, function(res) {
						//console.log("Status code of the Update call: ", res.statusCode);
						//console.log("PATH: "+pathUpdate);
						//console.log("PATH: "+updateBody);
						res.on('data', function(d) {
							resultUpdate +=d;
						});
						res.on('end', function() {
							if(res.statusCode==500 ||res.statusCode==400 || res.statusCode==401){
								//The sessionKey is not valid,
								//Trying to regenerate the sessionKey
								node.warn("No active sessionKey stored. Sending JOIN message...");
								var pathJoin = ssapResourceGenerator.generateJoinByTokenResource(baseUrl, token, clientPlatform, instance);

								// do the JOIN POST call
								var headersJoin = {
									'Accept' : 'application/json'
								};
								var optionsJoin = {
									host: host,
									//port: port,
									path: pathJoin,
									method: 'GET',
									headers: headersJoin,
									rejectUnauthorized: false
								};

								var resultJoin='';
								var reqJoin = http.request(optionsJoin, function(res) {
									//console.log("Status code of the Join call: ", res.statusCode);
									res.on('data', function(d) {
										resultJoin +=d;
									});
									res.on('end', function() {
										resultJoin = JSON.parse(resultJoin);
										server.sessionKey=resultJoin.sessionKey;
										node.log("Using SessionKey:"+server.sessionKey);

										if (updateType === "UPDATE"){
											var pathUpdate = ssapResourceGenerator.generateUpdateResource(baseUrl, ontology, query);
											var methodUpdate = "GET";
											var headersUpdate = {
												'Content-Type' : 'application/json',
												'Accept' : 'application/json',
												'Authorization' : server.sessionKey
											};
										}else if(updateType === "UPDATE_BY_ID"){ // The id to be removed is on the query variable.
											var pathUpdate = ssapResourceGenerator.generateUpdateByIdResource(baseUrl, ontology, id);
											var methodUpdate = "PUT";
											var headersUpdate = {
												'Content-Type' : 'application/json',
												'Accept' : 'application/json',
												'Authorization' : server.sessionKey,
												'Content-Length' : Buffer.byteLength(updateBody, 'utf8')
											};
										}else{
											node.error("Error: Update message type not supported");
										}

										var optionsUpdate = {
										  host: host,
										  path: pathUpdate,
										  method: methodUpdate,
										  headers: headersUpdate,
										  rejectUnauthorized: false
										};

										// do the UPDATE GET call
										var resultUpdate='';
										var reqUpdate = http.request(optionsUpdate, function(res) {
											//console.log("Status code of the Update call: ", res.statusCode);
											res.on('data', function(d) {
												resultUpdate +=d;
											});
											res.on('end', function() {
												if(res.statusCode=="200"){
													try{
														resultUpdate = JSON.parse(resultUpdate);
														msg.payload=resultUpdate;
													} catch (err) {
														msg.payload=resultUpdate;
														console.log("Error:"+err);
														node.error("Error:"+err);
													}
													node.send(msg);
													node.log(JSON.stringify(msg.payload));
												}
											});

										});
										reqUpdate.write(updateBody);
										reqUpdate.end();
										reqUpdate.on('error', function(err) {
											console.log("There was an error updating the data: ", err);
										});
									});
								});
								reqJoin.write(pathJoin);
								reqJoin.end();
								reqJoin.on('error', function(err) {
									console.log("There was an error updating the data: ", err);
									console.log("Error:"+err);
									node.error("Error:"+err);
								});
							} // end if 400 o 401 o 500
							else if(res.statusCode=="200"){
								//console.log("The data has been updated.");
								try{
									resultUpdate = JSON.parse(resultUpdate);
									msg.payload=resultUpdate;
								} catch (err) {
									msg.payload=resultUpdate;
									node.error(err);
								}
								node.send(msg);
							}
						});

					});
					reqUpdate.write(updateBody);
					reqUpdate.end();
					reqUpdate.on('error', function(err) {
						console.log("There was an error updating the data: ", err);
					});
				}
			} else {
				console.log("Error:"+err);
				node.error("Error:"+err);
			}
    });

  }
  RED.nodes.registerType("onesait-platform-update",Update);
}
