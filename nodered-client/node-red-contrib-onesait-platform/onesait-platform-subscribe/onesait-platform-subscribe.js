module.exports = function(RED) {
	var ssapMessageGenerator = require('../libraries/SSAPMessageGenerator');
	var sofiaConfig = require('../onesait-platform-connection-config/onesait-platform-connection-config');
	var ssapResourceGenerator = require('../libraries/SSAPResourceGenerator');
	//var http = require('http');
	//var https = require('https');
	var http = null;
	var isHttps = false;

    function Subscribe(n) {
      RED.nodes.createNode(this,n);

			var node = this;
			this.ontology = n.ontology;
			this.query = n.query;
			this.queryType = n.queryType;

			node.subscriptionId="";

			// Retrieve the server (config) node
			var server = RED.nodes.getNode(n.server);

			this.on('input', function(msg) {
				var ontologia="";
				var queryType="SQL";

				if(this.ontology==""){
				   ontologia = msg.ontology;
				}else{
				   ontologia=this.ontology;
				}

				var query="select * from "+ontologia;
				if (server) {
					var protocol = server.protocol;
					if(protocol.toUpperCase() == "MQTT".toUpperCase()){
						if (server.sessionKey==null || server.sessionKey=="")	{
							node.warn("No active sessionKey stored. Sending JOIN message...");
							var join = ssapMessageGenerator.generateJoinByTokenMessage(server.token, server.device, server.instance);
							var joinState = server.sendToSib(join);
							joinState.then(function(joinResponse){
								if (joinResponse.sessionKey !== null) {
									server.sessionKey = joinResponse.sessionKey;
									node.log("Using sessionKey:"+server.sessionKey);
									var subscribeQuery = ssapMessageGenerator.generateSubscribeWithQueryTypeMessage(query, ontologia, queryType, server.sessionKey);
									var state = server.sendToSib(subscribeQuery);
					
									state.then(function(response){
										var body = response.body;
										if(body.ok){
											msg.payload=body.data;
											node.send(msg);
											node.log(JSON.stringify(msg.payload));
											
											var sResponse;
											server.setNotification(function(sResponse){
												node.subscriptionId = sResponse.subsciptionId;
												msg.payload = sResponse;
												node.send(msg);
											}, server.sessionKey);
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
							
							var subscribeQuery = ssapMessageGenerator.generateSubscribeWithQueryTypeMessage(query, ontologia, queryType, server.sessionKey);
							var state = server.sendToSib(subscribeQuery);
							state.then(function(response){
								var body = response.body;
								if(body.ok){
									msg.payload=body.data;
									node.send(msg);
									node.log(JSON.stringify(msg.payload));
									
									var sResponse;
									server.setNotification(function(sResponse){
										node.subscriptionId = sResponse.subsciptionId;
										msg.payload = sResponse;
										node.send(msg);
									}, server.sessionKey);
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
													
													var subscribeQuery = ssapMessageGenerator.generateSubscribeWithQueryTypeMessage(query, ontologia, queryType, server.sessionKey);
													var state = server.sendToSib(subscribeQuery);

													state.then(function(response){
														var body = response.body;
														if(body.ok){
															msg.payload=body.data;
															node.send(msg);
															node.log(JSON.stringify(msg.payload));
															var sResponse;
															server.setNotification(function(sResponse){
																node.subscriptionId = sResponse.subsciptionId;
																msg.payload = sResponse;
																node.send(msg);
															}, server.sessionKey);
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
					else {
						node.error("Protocol not supported. MQTT should be used.");
					}
	      };

    	});

			//Invoqued on close or deploy flow.
			this.on('close', function() {

				// The System unsubscribes. the sessionKey could be different
				var queryUnsubscribe = ssapMessageGenerator.generateUnsubscribeMessage(node.subscriptionId, server.sessionKey);
				//console.log("Using query:"+queryUnsubscribe);
				if(server){
					var protocol = server.protocol;
					if(protocol.toUpperCase() == "MQTT".toUpperCase()){
						var state = server.sendToSib(queryUnsubscribe);

						state.then(function(response){
							if(response.body.ok){
								node.log("Successfully unsuscribed from ssKey: " + server.sessionKey);
								myDevice.disconnect(); // TODO opcional, solo quiero desuscribirme, pero al ser on 'close' igual es necesario la desconexion
																				 // (que hace obligatoriamente el s4c config)
							}else{
								console.log("Error sending the leave SSAP message.");
								if(response.body.errorCode == "AUTHENTICATION"){
									console.log("The sessionKey is not valid.");
								}
							}
						});
					}
					else{
						node.error("Protocol not supported. MQTT should be used.");
					}
				}
			});

		}
  RED.nodes.registerType("onesait-platform-subscribe",Subscribe);
}
