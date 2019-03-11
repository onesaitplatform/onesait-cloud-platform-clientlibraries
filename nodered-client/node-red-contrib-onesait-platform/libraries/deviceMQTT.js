/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 *
 * All rights reserved
 ******************************************************************************/
var mqtt   = require("mqtt");
var Q      = require('q');
var fs		 = require('fs');

// CHECK
//var mqtt   = require("mqtt@0.3.11");
//var Q      = require('q');
//var XXTEA  = require('./XXTEA');
//var Base64 = require('./base64');

var CLIENT_TOPIC                      = "/queue/message";    // Topic to publish messages
var TOPIC_PUBLISH_PREFIX              = '/topic/message/';    // Topic to receive the response
var TOPIC_SUBSCRIBE_INDICATION_PREFIX = '/topic/subscription/'; // Topic to receive notifications


function nop() {}

/**
 * Constructor
 */
function deviceMQTT() {
	this.notificationCallback = [];
	this.client = null;
	this.subscriptionsPromises = [];
	//this.cipherKey = null;
}


deviceMQTT.prototype.createUUID = function () {
    // http://www.ietf.org/rfc/rfc4122.txt
    var s         = [];
    var hexDigits = "0123456789abcdef";

    for (var i = 0; i < 23; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8]  = s[13] = s[18] = "-";

	  return s.join("");
};

/**
 * Connect to IoTBroker and subscribe to topics
 */
deviceMQTT.prototype.connect = function(host, port, path, keepalive) {
	keepalive = keepalive || 5;

	var clientId = this.createUUID();

	// New version, 3.1.1 style:
	var h = host;
	var p = port;
	var ssKey;
	var hasCertificate = (path==="")? false : true;
	if (hasCertificate) {
		var certData = fs.readFileSync(path);
		var opts = {
			port : p,
			host : h,
	    clientId  : clientId,
	    keepalive : keepalive,
			connectTimeout: 5000,
			clean: true,
			reconnectPeriod: -1,
			cert: certData,
			rejectUnauthorized: false,
			protocolId: 'MQIsdp',
			protocolVersion: 3,
			protocol: 'ssl'
	  };
	} else {
		var opts = {
			port : p,
			host : h,
	    clientId  : clientId,
	    keepalive : keepalive,
			connectTimeout: 5000,
			clean: true,
			reconnectPeriod: -1,
			protocolId: 'MQIsdp',
			protocolVersion: 3
	  };
	}

	this.client = mqtt.connect(opts);

	var self = this;

	this.client.on('connect', function(){
		self.client.subscribe(TOPIC_PUBLISH_PREFIX + clientId,
			function(){
				//console.log("[ Test ] Subscribed to "+TOPIC_PUBLISH_PREFIX + clientId)
			}
		);
		// self.client.subscribe(TOPIC_SUBSCRIBE_INDICATION_PREFIX + clientId,
		// 	function(){
		// 		console.log("[ Test ] Subscribed to "+TOPIC_SUBSCRIBE_INDICATION_PREFIX + ssKey)
		// 	});
	});


	this.client.on('message', function(topic, message) {
		if (topic == TOPIC_PUBLISH_PREFIX + clientId) {
			var deferred = self.subscriptionsPromises.shift();
			try {
				deferred.resolve(JSON.parse(message));
			} catch (e) {
				deferred.reject(e);
			}
		} else if (topic == TOPIC_SUBSCRIBE_INDICATION_PREFIX + ssKey) {
			var notifMsg = JSON.parse(message);
			var sMsgId = notifMsg.sessionKey;
			self.notificationCallback[sMsgId](notifMsg.body);
		}

		if (ssKey == null) {
 			ssKey = JSON.parse(message).sessionKey;
			self.client.subscribe(TOPIC_SUBSCRIBE_INDICATION_PREFIX + ssKey, function(){
					//console.log("Subscribed to: "+TOPIC_SUBSCRIBE_INDICATION_PREFIX + ssKey)
			});
		}

	});


};

deviceMQTT.prototype.disconnect = function() {
	self.client.unsubscribe(TOPIC_SUBSCRIBE_INDICATION_PREFIX + ssKey, function(){
	});
	this.client.end();
};


deviceMQTT.prototype.isConnected = function() {
	return this.client.connected;
};

deviceMQTT.prototype.send = function(ssapMessage) {

	var deferred = Q.defer();
	var self = this;
	this.client.publish(CLIENT_TOPIC, ssapMessage, {qos: 1, retain: false}, function() {
		self.subscriptionsPromises.push(deferred);
	});

	return deferred.promise;
};

// modified to manage subscriptionId parameter (in order to store different callbacks for each subscription)
deviceMQTT.prototype.setNotificationCallback = function(notificationCallback, subscriptionId) {
	if (typeof notificationCallback !== 'function') {
		throw new Error("notificationCallback must be a function");
	}
	this.notificationCallback[subscriptionId] = notificationCallback;
};



exports.deviceMQTT = deviceMQTT;
