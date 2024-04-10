function OPClient() {
			
		var _this = this;
		var config = null;
		var stompClient;
		var sessionKey;
		var queuePromises;
		var version = "";
		var status;
		var clientId="";
		
		
		this.configure = function (config) {
			_this.config = config;
		}
		
		this.onConnect = function (frame, callback) {
			console.log('Connected: ' + frame);
			_this.status = 'CONNECTED';
			_this.join(callback);
		}
		
		this.onError = function (error) {
			console.log('error: ' + error);
		}
		
		this.connect = function (callback) {
		
			if (_this.config === null) {
				throw Error('Configuration required!');
			}
			
			_this.status = 'CONNECTING';
			_this.queuePromises = [];
			
			
			var dfd = new Promise (function(resolve, reject){
			
				var socket = new SockJS(_this.config.url);
				
				_this.stompClient = Stomp.over(socket);
				
				if (_this.config.debug) {
					socket.debug = function(str) {
						console.log(str);
					};
					
					_this.stompClient.debug = function (str) {
						console.log(str);
					};
					
				}else {
					_this.stompClient.debug = false;
				}
				
				_this.stompClient.connect({}, 
						function (frame) {
							clientId=getClientIdFromURL(socket._transport.url);
							resolve(_this.onConnect(frame,callback));
						}, 
						function (err) {
							reject(_this.onError(err));
							
						}
				);
			});
			
			return dfd;		
			
		}
		
		var parseResponseWithACK = function (response, callback) {
		
			_this.stompClient.send("/stomp/ack/" + JSON.parse(response.body).messageId, {}, "");
		
			parseResponse (response, callback);
		}
		
		
		var parseResponse = function (response, callback) {
		
			response.body = response.body.replace(/"{/g, '{');
			response.body = response.body.replace(/}\"/g, '}');
			response.body = response.body.replace(/\\"/g, '"');
			
			try {
			
				body = JSON.parse(response.body);
				
				if(body.messageType =="JOIN"){
					sessionKey = body.sessionKey;
				}
			
			} catch (err){
				body = response;
			}
			
			if(callback)
			  return (callback(body));
			else
			  return body;
			  
			susbcription.unsubscribe();  
		}
		
		var getClientIdFromURL = function (str){
			str=str.substring(0, str.lastIndexOf("/"));
			return(str.substring(str.lastIndexOf("/")+1,str.length))
		}
		
		var escapeDoubleQuotes = function (str) {
			return str.replace(/\\([\s\S])|(")/g,"\\$1$2");
		}
		
		var sendMessage = function (message, callback) {
		
			var UUID = (new Date()).getTime();
			
			var dfd = new Promise (function(resolve){
			
				var susbcription = _this.stompClient.subscribe('/topic/message/' + UUID, function(response) {					
						resolve(parseResponse(response,callback));
				});

				_this.stompClient.send("/stomp/message/" + UUID, {}, message);
			
			});
			
			return dfd;
			
		}
		
		this.join = function (callback) {
		
			messageId = "";
			var joinMessage = '{'
							+ '"messageId":"' 
							+ messageId
							+ '","sessionKey":' 
							+ ((this.sessionKey != null)?'"' + this.sessionKey + '"': null)
							+ ',"direction":"REQUEST","messageType":"JOIN"'
							+ ',"body": {'
							+ '"@type": "SSAPBodyJoinMessage",'
							+ '"token":"'
							+ this.config.token
							+ '","deviceTemplate":"'
							+ this.config.deviceTemplate
							+ '","device":"'
							+ this.config.device
							+ '"'
							+ '}'
							+ '}';
			
			return sendMessage(joinMessage, callback);
			
		};
		
		this.leave = function () {
		
			if(stompClient != null) {
				stompClient.disconnect();
			}
			//setConnected(false);
			console.log("Disconnected");
		};
		
		this.query = function (ontology, query, queryType, callback) {
			messageId = "";
			var queryMessage = '{'
							+ '"messageId":"' 
							+ messageId
							+ '","sessionKey":' 
							+ ((sessionKey != null)?'"' + sessionKey + '"': null)
							+ ',"direction":"REQUEST","messageType":"QUERY"'
							+ ',"body": {'
							+ '"@type": "SSAPBodyQueryMessage",'
							+ '"ontology":"'
							+ ontology
							+ '","query":"'
							+ escapeDoubleQuotes(query)
							+ '","queryType":"'
							+ queryType
							+ '"'
							+ '}'
							+ '}';
											
			return sendMessage(queryMessage, callback);
			
		}
		
		this.insert = function (ontology, data, callback) {
			messageId = "";
			var insertMessage = '{'
							+ '"messageId":"' 
							+ messageId
							+ '","sessionKey":' 
							+ ((sessionKey != null)?'"' + sessionKey + '"': null)
							+ ',"direction":"REQUEST","messageType":"INSERT"'
							+ ',"body": {'
							+ '"@type": "SSAPBodyInsertMessage",'
							+ '"ontology":"'
							+ ontology
							+ '","data":'
							+ data
							+ ''
							+ '}'
							+ '}';
											
			return sendMessage(insertMessage, callback);
		};
		
		this.update = function (ontology, query, callback) {
		
			messageId = "";
			var updateMessage = '{'
								+ '"messageId":"' 
								+ messageId
								+ '","sessionKey":' 
								+ ((sessionKey != null)?'"' + sessionKey + '"': null)
								+ ',"direction":"REQUEST","messageType":"UPDATE"'
								+ ',"body": {'
								+ '"@type": "SSAPBodyUpdateMessage",'
								+ '"ontology":"'
								+ ontology
								+ '","query":"'
								+ escapeDoubleQuotes(query)
								+ '"}'
								+ '}';
							
						
			return sendMessage(updateMessage, callback);
			
		};
		
		this.updateById = function (ontology, id, data, callback) {

			messageId = "";
			var updateMessage = '{'
								+ '"messageId":"' 
								+ messageId
								+ '","sessionKey":' 
								+ ((sessionKey != null)?'"' + sessionKey + '"': null)
								+ ',"direction":"REQUEST","messageType":"UPDATE_BY_ID"'
								+ ',"body": {'
								+ '"@type": "SSAPBodyUpdateByIdMessage",'
								+ '"ontology":"'
								+ ontology
								+ '","id":"'
								+ id
								+ '","data":'
								+ data
								+ ''
								+ '}'
								+ '}';
															
			return sendMessage(updateMessage, callback);
		};
		
		this.remove = function (ontology, query, callback) {
			
			messageId = "";
			var deleteMessage = '{'
								+ '"messageId":"' 
								+ messageId
								+ '","sessionKey":' 
								+ ((sessionKey != null)?'"' + sessionKey + '"': null)
								+ ',"direction":"REQUEST","messageType":"DELETE"'
								+ ',"body": {'
								+ '"@type": "SSAPBodyDeleteMessage",'
								+ '"ontology":"'
								+ ontology
								+ '","query":"'
								+ escapeDoubleQuotes(query)
								+ '"'
								+ ''
								+ '}'
								+ '}';
															
			return sendMessage(deleteMessage, callback);
		};
		
		this.removeById = function (ontology, id, callback) {
			
			messageId = "";
			var deleteMessage = '{'
								+ '"messageId":"' 
								+ messageId
								+ '","sessionKey":' 
								+ ((sessionKey != null)?'"' + sessionKey + '"': null)
								+ ',"direction":"REQUEST","messageType":"DELETE_BY_ID"'
								+ ',"body": {'
								+ '"@type": "SSAPBodyDeleteByIdMessage",'
								+ '"ontology":"'
								+ ontology
								+ '","id":"'
								+ id
								+ '"'
								+ '}'
								+ '}';
															
			return sendMessage(deleteMessage, callback);
		};

		this.subscribe = function (subscription, queryValue, queryType, callbackOperation, callbackSubscriptionMessages){

			messageId = "";
			var subscribeMessage = '{'
							+ '"messageId":"' 
							+ messageId
							+ '","sessionKey":' 
							+ ((sessionKey != null)?'"' + sessionKey + '"': null)
							+ ',"direction":"REQUEST","messageType":"SUBSCRIBE"'
							+ ',"body": {'
							+ '"@type": "SSAPBodySubscribeMessage",'
							+ '"subscription":"'
							+ subscription
							+ '","queryValue":"'
							+ escapeDoubleQuotes(queryValue)
							+ '","queryType":"'
							+ queryType
							+ '","clientId":"'
							+ clientId
							+ '"'
							+ '}'
							+ '}';

			this.stompClient.subscribe("/topic/subscription/" + sessionKey, function(response){
				parseResponseWithACK(response, callbackSubscriptionMessages)});

			return sendMessage(subscribeMessage, callbackOperation);
		};
		
		
		this.unsubscribe = function (subscription, callbackOperation){

			messageId = "";
			var unsubscribeMessage = '{'
							+ '"messageId":"' 
							+ messageId
							+ '","sessionKey":' 
							+ ((sessionKey != null)?'"' + sessionKey + '"': null)
							+ ',"direction":"REQUEST","messageType":"UNSUBSCRIBE"'
							+ ',"body": {'
							+ '"@type": "SSAPBodyUnsubscribeMessage",'
							+ '"subscriptionId":"'
							+ subscription
							+ '","clientId":"'
							+ clientId
							+ '"'
							+ '}'
							+ '}';

			return sendMessage(unsubscribeMessage, callbackOperation);
		};
		
}
			