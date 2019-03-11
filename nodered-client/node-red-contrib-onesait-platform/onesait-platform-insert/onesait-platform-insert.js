module.exports = function(RED) {
	var ssapMessageGenerator = require('../libraries/SSAPMessageGenerator');
	var device = require('../libraries/deviceMQTT');
	var ssapResourceGenerator = require('../libraries/SSAPResourceGenerator');
	var http = null;
	var isHttps = false;

  function Insert(n) {
    RED.nodes.createNode(this,n);
    var node = this;
		this.ontology = n.ontology;

		// Retrieve the server (config) node
	 	var server = RED.nodes.getNode(n.server);

		this.on('input', function(msg) {
			var ontology="";
			var sessionKey='';
			if(this.ontology=="") {ontology = msg.ontology;}
				else {ontology=this.ontology;}

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
								var queryAfterJoin = ssapMessageGenerator.generateInsertMessage(JSON.stringify(msg.payload), ontology,server.sessionKey);
								var state = server.sendToSib(queryAfterJoin);


								state.then(function(response){
									var body = response.body;
									if(body.ok){
										msg.payload=body.data;
										node.send(msg);
										node.log(JSON.stringify(msg.payload));
									}else{
										node.warn("Error sending the query SSAP message by:"+body.error);
										msg.payload=body;
										if(body.errorCode == "AUTHORIZATION"){
											node.warn("Error: The sessionKey is not valid. Generating new Session....");
											server.sessionKey="";
											server.generateSession();
										}
										node.error(msg);
									}
								});
							} else {//Sobre todo renovación de sesión
								//check exception management etc.
								node.error("No sessionKey retrieved");
							}
						});
					}
					else{
						node.log("Using sessionKey:"+server.sessionKey);
						var queryInsert = ssapMessageGenerator.generateInsertMessage(JSON.stringify(msg.payload), ontology,server.sessionKey);
						var state = server.sendToSib(queryInsert);
						if(typeof(state)=="undefined" || state==""){
							console.log("There's no response for the query send.");
						}else{
							state.then(function(response){

								var body = response.body;
								//console.log("Response Body:"+body);
								if(body.ok){
									//console.log("Message sent OK.");
									msg.payload=body.data;
									node.send(msg);
									node.log(JSON.stringify(msg.payload));

								}else{
									console.log("Error sending SSAP message:"+body.error);
									msg.payload=body.error;

									if(body.errorCode == "AUTHORIZATION"){
										node.warn("Error: The sessionKey is not valid. Generating new Session....");
										var join = ssapMessageGenerator.generateJoinByTokenMessage(server.token, server.device, server.instance);
										var joinState = server.sendToSib(join);
									
										joinState.then(function(joinResponse){
											if (joinResponse.sessionKey !== null) {
												server.sessionKey = joinResponse.sessionKey;
												node.log("Using sessionKey:"+server.sessionKey);
												var queryAfterJoin = ssapMessageGenerator.generateInsertMessage(JSON.stringify(msg.payload), ontology,server.sessionKey);
												var state = server.sendToSib(queryAfterJoin);


												state.then(function(response){
													var body = response.body;
													if(body.ok){
														msg.payload=body.data;
														node.send(msg);
														node.log(JSON.stringify(msg.payload));
													}else{
														node.warn("Error sending the query SSAP message by:"+body.error);
														msg.payload=body;
														if(body.errorCode == "AUTHORIZATION"){
															node.warn("Error: The sessionKey is not valid. Generating new Session....");
															server.sessionKey="";
															server.generateSession();
														}
														node.error(msg);
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
				}
				else if(protocol.toUpperCase() == "REST".toUpperCase()){

					var endpoint = server.endpoint;
					var arr = endpoint.toString().split(":");
					var clientPlatform = server.device;
					var instance = server.instance;
					var token = server.token;
					var hostProtocol = arr[0];
					var host;
					//var port = 19000;
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
					var pathInsert = ssapResourceGenerator.generateInsertResource(baseUrl, ontology);

					var insertBody = JSON.stringify(msg.payload);
					var headersInsert = {
						'Content-Type' : 'application/json',
						'Accept' : 'application/json',
						'Authorization' : server.sessionKey,
						'Content-Length' : Buffer.byteLength(insertBody, 'utf8')
					};
					var optionsInsert = {
					  host: host,
					  path: pathInsert,
					  method: 'POST',
					  headers: headersInsert,
						rejectUnauthorized: false
					};

					// do the INSERT POST call
					var resultInsert='';
					if (isHttps) {http= require('https');}
					else {http = require('http');}

					var reqInsert = http.request(optionsInsert, function(res) {
						//console.log("Status code of the Insert call: ", res.statusCode);
						res.on('data', function(d) {
							resultInsert +=d;
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
									path: pathJoin,
									method: 'GET',
									headers: headersJoin,
									rejectUnauthorized: false
								};

								var resultJoin='';
								var reqPost = http.request(optionsJoin, function(res) {
									//console.log("Status code of the Join call: ", res.statusCode);
									res.on('data', function(d) {
										resultJoin +=d;
									});
									res.on('end', function() {
										resultJoin = JSON.parse(resultJoin);
										server.sessionKey=resultJoin.sessionKey;
										node.log("Using sessionKey:"+server.sessionKey);

										var pathInsert = ssapResourceGenerator.generateInsertResource(baseUrl, ontology);

										var headersInsert = {
											'Content-Type' : 'application/json',
											'Accept' : 'application/json',
											'Authorization' : server.sessionKey,
											'Content-Length' : Buffer.byteLength(insertBody, 'utf8')
										};
										var optionsInsert = {
										  host: host,
										  path: pathInsert,
										  method: 'POST',
										  headers: headersInsert,
											rejectUnauthorized: false
										};

										var resultInsert='';
										var reqInsert = http.request(optionsInsert, function(res) {
											//console.log("Status code of the Insert call: ", res.statusCode);
											res.on('data', function(d) {
												resultInsert +=d;
											});
											res.on('end', function() {
												try{
													resultInsert = JSON.parse(resultInsert);
													msg.payload=resultInsert;
												} catch (err) {
													msg.payload=resultInsert;
													console.log("Error:"+err);
													node.error("Error:"+err);
												}
												node.send(msg);
												node.log(JSON.stringify(msg.payload));
											});


										});
										reqInsert.write(insertBody);
										reqInsert.end();
										reqInsert.on('error', function(err) {
											console.log("Error:"+err);
											node.error("Error:"+err);
										});
									});

								});
								reqPost.write(pathJoin);
								reqPost.end();
								reqPost.on('error', function(err) {
									console.log("There was an error inserting the data");
									console.log("Error:"+err);
									node.error("Error:"+err);
								});

							}	else if(res.statusCode==200){
								try{
									resultInsert = JSON.parse(resultInsert);
									msg.payload=resultInsert;
								} catch (err) {
									msg.payload=resultInsert;
									console.log("Error:"+err);
									node.error("Error:"+err);
								}
								node.send(msg);
							}
						});

					});
					reqInsert.write(insertBody);
					reqInsert.end();
					reqInsert.on('error', function(err) {
						console.log(err);
						console.log("Error:"+err);
						node.error("Error:"+err);
					});
				}

			} else {
				console.log("Error:"+err);
				node.error("Error:"+err);
			}
    });

  }
  RED.nodes.registerType("onesait-platform-insert",Insert);
}
