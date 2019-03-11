module.exports = function(RED) {
	var ssapMessageGenerator = require('../libraries/SSAPMessageGenerator');
	var sofiaConfig = require('../onesait-platform-connection-config/onesait-platform-connection-config');
	var ssapResourceGenerator = require('../libraries/SSAPResourceGenerator');
	var http = null;
	var isHttps = false;

  function Leave(n) {
    RED.nodes.createNode(this,n);
    var node = this;

		// Retrieve the server (config) node
		var server = RED.nodes.getNode(n.server);

		this.on('input', function(msg) {
			if (server) {
				if (server.sessionKey==""){
					node.error("No sessionKey stored");
				}
				else{
					var protocol = server.protocol;
					if(protocol.toUpperCase() == "MQTT".toUpperCase()){
						var queryLeave = ssapMessageGenerator.generateLeaveMessage(server.sessionKey);
						//console.log("Using query:"+queryLeave);
						var state = server.sendToSib(queryLeave);
						//console.log("SessionKey leaved: " + server.sessionKey);


						state.then(function(response){

							var body = response.body;
							if(body.ok){
								//console.log("Message sent");
								msg.payload = "The session: " +server.sessionKey+ " is now closed";
								server.sessionKey="";
								node.send(msg);
								node.log(msg.payload);
							}else{
								//console.log("Error sending the leave SSAP message.");
								msg.payload=body.error;
								if(body.errorCode == "AUTENTICATION"){
									//console.log("The sessionKey is not valid.");
									node.error("The sessionKey is not valid");
									// server.generateSession();
								}
								node.send(msg);
								node.error(msg.payload);
							}
						});
					}
					else if(protocol.toUpperCase() == "REST".toUpperCase()){

						var endpoint = server.endpoint;
						var arr = endpoint.toString().split(":");
						var clientPlatform = server.device;
						var instance = server.instance;
						var token = server.token;
						var hostProtocol = arr[0];
						var host;
						// console.log("[ REST ] Endpoint:"+endpoint);
						// console.log("[ REST ] device:"+clientPlatform);
						// console.log("[ REST ] Instance:"+instance);
						// console.log("[ REST ] token:"+token);
						// console.log("[ REST ] Using "+arr[0]);

						if (arr[0].toUpperCase()=='HTTPS'.toUpperCase()) {
							isHttps=true;
							console.log("Using HTTPS:"+arr[0]);
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
						var pathLeave = ssapResourceGenerator.generateLeaveResource(baseUrl, server.sessionKey);

						var headersLeave = {
									'Content-Type' : 'application/json',
									'Accept' : 'application/json',
									'Authorization' : server.sessionKey
						};
						var optionsLeave = {
						  host: host,
						  path: pathLeave,
						  method: 'GET',
						  headers: headersLeave,
						  rejectUnauthorized: false
						};

						// do the LEAVE POST call
						var resultLeave='';
						if (isHttps) {http= require('https');}
						else {http = require('http');}

						var reqLeave = http.request(optionsLeave, function(res) {
							//console.log("Status code of the Leave call: ", res.statusCode);
							res.on('data', function(d) {
								resultLeave +=d;
							});
							res.on('end', function() {
								if(res.statusCode=="200"){
									//console.log("The session is closed.");
									msg.payload = "The session: " +server.sessionKey+ " is now closed";
									node.send(msg);
									node.log(msg.payload);
								}
							});

						});
						reqLeave.write(pathLeave);
						reqLeave.end();
						reqLeave.on('error', function(err) {
							//console.log("Error:"+err);
							node.error("Error:"+err);
						});
					}
					else if(protocol.toUpperCase() == "WEBSOCKET".toUpperCase()){
							//console.log("TODO");
							node.error("TODO");
					}
				}


			} else {
					console.log("Error:"+err);
					node.error("Error:"+err);
			}

    });

  }
  RED.nodes.registerType("onesait-platform-leave",Leave);
}
