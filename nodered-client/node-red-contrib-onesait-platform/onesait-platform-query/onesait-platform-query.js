module.exports = function(RED) {
	var ssapMessageGenerator = require('../libraries/SSAPMessageGenerator');
	var sofiaConfig = require('../onesait-platform-connection-config/onesait-platform-connection-config');
	var ssapResourceGenerator = require('../libraries/SSAPResourceGenerator');
	//var http = require('http');
	//var https = require('https');
	var http = null;
	var isHttps = false;

  function Query(n) {
    RED.nodes.createNode(this,n);

		var node = this;
		this.ontology = n.ontology;
		this.query = n.query;
		this.queryType = n.queryType;

		// Retrieve the server (config) node
		var server = RED.nodes.getNode(n.server);

		this.on('input', function(msg) {
			var ontology="";
			var queryType="";
			var query="";
			if(this.ontology=="") {ontology = msg.ontology;}
				else {ontology=this.ontology;}
			if(this.queryType=="") {queryType = msg.queryType;}
				else {queryType=this.queryType;}
			if(this.query=="") {query = msg.query;}
				else {query=this.query;}

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
								var queryAfterJoin = ssapMessageGenerator.generateQueryWithQueryTypeMessage(query, ontology, queryType, server.sessionKey);
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
							//console.log("Using SessionKey:"+server.sessionKey);
						node.log("Using sessionKey:"+server.sessionKey);
						var query = ssapMessageGenerator.generateQueryWithQueryTypeMessage(query, ontology, queryType, server.sessionKey);
						var state = server.sendToSib(query);

						state.then(function(response){
							console.log(""+JSON.stringify(response));
							var body = response.body;
							if(body.ok){
								msg.payload=body.data;
								node.send(msg);
								node.log(JSON.stringify(msg.payload));
							}else{
								node.warn("Error sending the query SSAP message by: "+body.error);
								msg.payload=body;
								if(body.errorCode == "AUTHORIZATION"){
									node.warn("Error: The sessionKey is not valid. Generating new Session....");
									//server.sessionKey="";
									//server.generateSession();
									var join = ssapMessageGenerator.generateJoinByTokenMessage(server.token, server.device, server.instance);
									var joinState = server.sendToSib(join);
									
									joinState.then(function(joinResponse){
										if (joinResponse.sessionKey !== null) {
											server.sessionKey = joinResponse.sessionKey;
											node.log("Using sessionKey:"+server.sessionKey);
											var queryAfterJoin = ssapMessageGenerator.generateQueryWithQueryTypeMessage(query, ontology, queryType, server.sessionKey);
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
								node.error(msg);
							}
						});
					}
					
				}
				else if(protocol.toUpperCase() == "REST".toUpperCase()){
					query = encodeURIComponent(query);
					var endpoint = server.endpoint;
					var arr = endpoint.toString().split(":");
					var clientPlatform = server.device;
					var instance = server.instance;
					var token = server.token;
					var hostProtocol = arr[0];
					var host;
					var port;
					// console.log("[ REST ] Endpoint:"+endpoint);
					// console.log("[ REST ] device:"+clientPlatform);
					// console.log("[ REST ] Instance:"+instance);
					// console.log("[ REST ] token:"+token);
					// console.log("[ REST ] Using "+arr[0]);

					// Check https/http
					if (arr[0].toUpperCase()=='HTTPS'.toUpperCase()) {
						isHttps=true;
						//console.log("Using HTTPS:"+arr[0]);
						//node.log("Using HTTPS:"+arr[0]);
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
					var pathQuery = ssapResourceGenerator.generateQueryResource(baseUrl, ontology, query, queryType);

					var headersQuery = {
						'Content-Type' : 'application/json',
						'Accept' : 'application/json',
						'Authorization' : server.sessionKey
					};
					var optionsQuery = {
					  host: host,
					  path: pathQuery,
					  method: 'GET',
					  headers: headersQuery,
					  rejectUnauthorized: false
					};
					
					// do the GET call
					var result='';
					if (isHttps) {http= require('https');}
					else {http = require('http');}

					var req = http.request(optionsQuery, function(res) {
						//console.log("Status code of the query call: ", res.statusCode);
						if( res.statusCode==400 || res.statusCode==401){
							node.warn("No active sessionKey stored. Sending JOIN message...");
							var pathJoin = ssapResourceGenerator.generateJoinByTokenResource(baseUrl, token, clientPlatform, instance);

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
							//console.log("LLAMADA:"+pathJoin);
							// do the JOIN call
							var resultJoin='';
							var reqPost = http.request(optionsJoin, function(res) {
								//console.log("Status code of the Join call: ", res.statusCode);
								res.on('data', function(d) {
									resultJoin +=d;
								});
								res.on('end', function() {
									resultJoin = JSON.parse(resultJoin);
									server.sessionKey=resultJoin.sessionKey;
									//console.log("SessionKey obtained: " + server.sessionKey);
									node.log("Using sessionKey: " + server.sessionKey);
									var pathQuery = ssapResourceGenerator.generateQueryResource(baseUrl, ontology, query, queryType);

									var headersQuery = {
										'Content-Type' : 'application/json',
										'Accept' : 'application/json',
										'Authorization' : server.sessionKey
									};
									var optionsQuery = {
									  host: host,
									  path: pathQuery,
									  method: 'GET',
									  headers: headersQuery,
								      rejectUnauthorized: false
									};
									// do the GET call
									var result='';
									var req = http.request(optionsQuery, function(res) {
										//console.log("Status code of the query call: ", res.statusCode);
										res.on('data', function(d) {
											result +=d;
										});
										res.on('end', function() {
											try {
												node.log(""+result);
												result = JSON.parse(result);
											} catch (err) {
												node.error("Error JSON.parse:"+err);
											}
											msg.payload=result;
											node.send(msg);
										});

									});
									req.end();
									req.on('error', function(err) {
										console.log("Error:"+err);
										node.error("Error:"+err);
									});
								});

							});
							reqPost.write(pathJoin);
							reqPost.end();
							reqPost.on('error', function(err) {
								node.error("There was an error inserting the data: ", err);
							});
						} else if(res.statusCode==200){
							res.on('data', function(d) {
								result +=d;
							});
							res.on('end', function() {
								node.log(""+result);
								result = JSON.parse(result);
								msg.payload=result;
								node.send(msg);
							});
						}
					});
					req.end();
					req.on('error', function(err) {
						console.log("Error:"+err);
						node.error("Error:"+err);
					});
					//console.log("Output:"+result);
				}

			} else {
				console.log("Error:"+err);
				node.error("Error:"+err);
			}
    });

  }
  RED.nodes.registerType("onesait-platform-query",Query);
}
