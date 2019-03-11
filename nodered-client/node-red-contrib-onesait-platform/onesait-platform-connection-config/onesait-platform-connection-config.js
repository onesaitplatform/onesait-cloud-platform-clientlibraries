module.exports = function(RED) {
	"use strict";
	var device = require('../libraries/deviceMQTT');
	var ssapMessageGenerator = require('../libraries/SSAPMessageGenerator')
	var waitUntil = require('wait-until');

	//Invoques constructor on deploy
  function SofiaConfig(n) {

    RED.nodes.createNode(this,n);
		// this.on('close', function () {
		// 	console.log('Closing MQTT connection...');
		// 	myDevice.disconnect();
		// 	console.log("Connection closed.");
		// });

    this.protocol=n.protocol;
		this.device=n.device;
		this.instance=n.instance;
		this.token=n.token;
		this.renovation=n.renovation;
		this.check=n.check;
		this.path=n.path;


		var node=this;
		node.sessionKey="";
		node.connected=false;

		var myDevice;

		//Stops the renew session interval
		var testConnectionInterval;


		if(this.protocol.toUpperCase() === "MQTT".toUpperCase()){
			this.ip=n.ip;
			this.port=n.port;
			if (!this.check) this.path=""; //Disable path if checkbox is not selected
			myDevice = new device.deviceMQTT();

			console.log("MQTT: Trying to connect on: "+this.ip+":"+this.port);

			//Connect to the IoTBroker
			myDevice.connect(this.ip, this.port, this.path);

			//Checks 5 times with an intervarl of x seconds if it has been connected (Definitely 5 seconds, the timeout of our deviceMQTT)
			waitUntil(1000, 5,
						function condition() {
							return myDevice.isConnected();
						},
						function done(result) {
							if(result){//Está conectado
								//Generate Sessionkey
								generateSession();
							}else{
								console.log("Unable to connect");
							}
						}
			);

			testConnectionInterval= setInterval( function() {
				if (myDevice != null && typeof(myDevice) != "undefined")  {
					if (!myDevice.isConnected()) {
						node.log("Physic reconnection");
						myDevice.connect(node.ip, node.port, node.path);
						waitUntil(1000, 5,
							function condition() {
								return myDevice.isConnected();
							},
							function done(result) {
								if(result){//Está conectado
									node.sessionKey=="";
									generateSession();
								}else{
									console.log("Error on Connection by: "+result);
								}
							}
						);

					}else{
						node.log("Device Connected. Renew Sessionkey");
						generateSession();
					}
				}
			}, this.renovation * 60000);	// retry every renovation minutes


		}else{

			this.endpoint=n.endpoint;

		}


		function generateSession () { 
			//console.log("The sessionKey is going to be generated.")
			if(typeof(myDevice) != "undefined"){
				var ssapMessageJOIN;

				if( typeof(node.sessionKey)=="undefined" || node.sessionKey==""){
					//console.log("There is no previous session, generate new session...")
					node.log("There is no previous session, generate new session...");
					ssapMessageJOIN = ssapMessageGenerator.generateJoinByTokenMessage(node.token, node.device, node.instance );
					//console.log('SSAPJOIN: ' + ssapMessageJOIN);
				}else{ //There is a previouse session. Try to renovate it
					//console.log("There is a previouse session. Try to renovate it...")
					ssapMessageJOIN = ssapMessageGenerator.generateJoinRenovateByTokenMessage(node.token, node.device, node.instance, node.sessionKey );
				}
				myDevice.send(ssapMessageJOIN)
					.then(function(joinResponse) {
						 //console.log('Response body: ' + JSON.stringify(joinResponse));
						if (joinResponse.sessionKey !== null) {
							node.sessionKey = joinResponse.sessionKey;
							node.connected = true;
							//console.log('Session created with iotBroker with sessionKey: ' + node.sessionKey);
							node.log('Session created with iotBroker with sessionKey: ' + node.sessionKey);
						} else {//Sobre todo renovación de sesión
							//check exception management etc.

							node.connected = false;
							node.sessionKey="";
						}
					})
					.done(function() {
						//console.log('Connection established with SessionKey:'+node.sessionKey);
					});
			}

		}

		function setNotification(func, subscribeId){
			myDevice.setNotificationCallback(func, subscribeId);
		}
		node.setNotification=setNotification;
		node.generateSession=generateSession;

		//Invoqued on close or deploy flow.
		this.on('close', function() {
			clearInterval(testConnectionInterval);

			//LEAVE and physical disconnect
			var queryLeave = ssapMessageGenerator.generateLeaveMessage(node.sessionKey);
			if(this.server=="undefined"){
				console.log("server: " + this.server);
				var state = this.server.sendToSib(queryLeave);

				state.then(function(response){

					var body = JSON.parse(response.body);
					if(body.ok){
						console.log("The message is send. Closing session");
						myDevice.disconnect();
					}else{
						console.log("Error sending the leave SSAP message.");
						if(body.errorCode == "AUTHENTICATION"){
							console.log("The sessionKey is not valid.");
							generateSession();
						}
					}
				});
			}
		});


		//Envia un mensaje al IoTBroker
		this.sendToSib=function(msg) {
		  if(typeof(myDevice) != "undefined"){
			return myDevice.send(msg);
		  }
		}
		//Devuelve la sessionkey de la conexión
		this.getSessionKey=function() {
		  return node.sessionKey;
		}
  }

  RED.nodes.registerType("onesait-platform-connection-config",SofiaConfig);
}
