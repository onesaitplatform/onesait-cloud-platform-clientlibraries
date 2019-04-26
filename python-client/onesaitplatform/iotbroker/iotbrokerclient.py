import time
import datetime
import requests
from string import Template
import json

try:
    from onesaitplatform.iotbroker.client import Client
    from onesaitplatform.iotbroker.utils import wait
    import onesaitplatform.common.config as config
    from onesaitplatform.common.log import log
    from onesaitplatform.enum import QueryType
except Exception as e:
    print("Error - Not possible to import necesary libraries: {}".format(e))


class IotBrokerClient(Client):
    """
    Class IotBrokerClient to connect with Iot-Broker of OnesaitPlatform
    """
    log.init_logging()

    protocol = config.PROTOCOL
    iot_broker_path = "/iot-broker"
    batch_size = config.BATCH_QUERY_SIZE

    join_template = Template("$protocol://$host$path/rest/client/join")
    leave_template = Template("$protocol://$host$path/rest/client/leave")
    query_template = Template("$protocol://$host$path/rest/ontology/$ontology")
    insert_template = Template("$protocol://$host$path/rest/ontology/$ontology")

    query_batch_sql = Template("$query offset $start limit $end")
    query_batch_mongo = Template("$query.skip($start).limit($end)")

    def __init__(self,
                 host=config.HOST,
                 iot_client=config.IOT_CLIENT,
                 iot_client_token=config.IOT_CLIENT_TOKEN):
        """
        Class IotBrokerClient to connect with Iot-Broker of OnesaitPlatform

        @param host               Onesaitplatform host
        @param iot_client         Onesaitplatform Iot-Client
        @param iot_client_token   Onesaitplatform iot-Client-Token
        """
        
        super().__init__(host=host)
        
        self.iot_client = iot_client
        self.iot_clientId = iot_client + ":PythonClient"
        self.iot_client_token = iot_client_token
        self.session_key = None

        log.info("Created connection with iot-broker \
                 host:{}, path:{}, client:{}, token:{}"
                 .format(host, self.iot_broker_path, 
                         iot_client, iot_client_token))
        self.add_to_debug_trace("Created connection with \
                                iot-broker host:{}, path:{}, \
                                client:{}, token:{}"
                                .format(host, self.iot_broker_path, 
                                        iot_client, iot_client_token))

    def __str__(self):
        """
        String to print object info
        """
        hide_attributes = ["debug_trace"]
        info = "{}.{}".format(Client.__name__, self.__class__.__name__)
        info += "("
        for k, v in self.__dict__.items():
            if k not in hide_attributes:
                info += "{}={}, ".format(k, v)
        info = info[:-2] + ")"
        
        return info

    def to_json(self, as_string=False):
        """
        Export object to json

        @param as_string    If json dumped (String)

        @return json_obj    json-dict/ json string
        """
        self.debug_trace = []
        json_obj = dict(self.__dict__)
        json_obj.pop("debug_trace", None)
        if as_string:
            json_obj = json.dumps(json_obj)

        log.info("Exported json {}".format(json_obj))
        self.add_to_debug_trace("Exported json {}".format(json_obj))
        return json_obj

    @staticmethod
    def from_json(json_object):
        """
        Creates a object from json-dict/ json-string

        @param json_object    json.dict/ json-string

        @return connection   connection object
        """
        connection = None
        try:
            if type(json_object) == str:
                json_object = json.loads(json_object)
            connection = IotBrokerClient(host=json_object['host'], 
                                         iot_client=json_object['iot_client'], 
                                         iot_client_token=json_object['iot_client_token'])
            connection.is_connected = json_object['is_connected']
            connection.session_key = json_object['session_key']
            log.info("Imported json {}".format(json_object))
            connection.add_to_debug_trace("Imported json {}".format(json_object))

        except Exception as e:
            log.error("Not possible to import object from json: {}".format(e))
        
        return connection
    
    # @wait(.1, .1)
    def join(self, iot_client=None, iot_client_token=None):
        """
        Login in the platform with Iot-Client credentials

        @return ok, info  
        """
        _ok = False
        _res = None

        if iot_client is not None:
            self.iot_client = iot_client
        if iot_client_token is not None:
            self.iot_client_token = iot_client_token

        try:
            log.info("Created connection with iot-broker host:{}, path:{}, client:{}, token:{}"
                     .format(self.host, self.iot_broker_path, self.iot_client, self.iot_client_token))

            url = self.join_template.substitute(protocol=self.protocol, host=self.host, path=self.iot_broker_path)
            querystring = {"token": self.iot_client_token, "clientPlatform": self.iot_client, "clientPlatformId": self.iot_clientId}
            headers = {'Accept': "application/json", 'Content-type': "application/json"}
            response = self.call_rest_API("GET", url, headers, querystring)
            _res = response.json()
            self.session_key = _res["sessionKey"]
            self.is_connected = True
            log.info("Logged correctly with session_key: {}".format(response.text))
            self.add_to_debug_trace("Logged correctly with session_key: {}".format(response.text))
            _ok = True

        except Exception as e:
            self.is_connected = False
            log.error("Not possible to conect with iot-broker: {}".format(e))

        return _ok, _res

    # @wait(.1, .1)
    def leave(self):
        """
        Logout in the platform with session token

        @return ok, info
        """
        _ok = False
        _res = None
        try:
            log.info("Leaving connection with session_key:{}".format(self.session_key))
            if self.is_connected:
                url = self.leave_template.substitute(protocol=self.protocol, host=self.host, path=self.iot_broker_path)
                headers = {'Authorization': self.session_key}
                response = self.call_rest_API("GET", url, headers=headers)
                _res = json.loads(response.text)
                self.is_connected = False
                self.session_key = None
                log.info("Disconnected correctly: {}".format(response.text))
                self.add_to_debug_trace("Disconnected correctly: {}".format(response.text))
            else:
                log.info("There is not connection, please join() before leave()")
                self.add_to_debug_trace("There is not connection, please join() before leave()")
            _ok = True

        except Exception as e:
            log.error("Not possible to disconnect with iot-broker: {}".format(e))
            self.add_to_debug_trace("Not possible to disconnect with iot-broker: {}".format(e))

        return _ok, _res

    # @wait(.1, .1)
    def restart(self):
        """
        Restar conection:
            - leave()
            - join()

        @return ok, info
        """
        log.info("Restarting connection with session_key:{}, connected:{}".format(self.session_key, self.is_connected))
        _ok_leave, _res_leave = self.leave()
        _ok_join, _res_join = self.join()
        if not _ok_join:
            log.warning("Not possible to restart connexion with iot-broker")
            self.add_to_debug_trace("Not possible to restart connexion with iot-broker")

        return _ok_join, _res_join

    # @wait(.1, .1)
    def query(self, ontology, query, query_type):
        """
        Make a query to iot-broker service of the platform

        @param ontology     ontology name
        @param query        query expression
        @param query_type    quert type ['NATIVE', 'SQL']

        @return ok, info
        """
        _ok = False
        _res = None
        try:
            log.info("Making query to ontology:{}, query:{}, query_type:{}".format(ontology, query, query_type))
            url = self.query_template.substitute(protocol=self.protocol, host=self.host, 
                                                 path=self.iot_broker_path, ontology=ontology)
            querystring = {"query": query, "queryType": query_type.upper()}
            headers = {'Authorization': self.session_key}
            response = self.call_rest_API("GET", url, headers=headers, params=querystring)
            log.info("Response: {} - {}".format(response.status_code, response.text))
            self.add_to_debug_trace("Response: {} - {}".format(response.status_code, response.text))

            if response.status_code != 200:
                log.info("Not possible to connect ({}) - {}, reconnecting...".format(response.status_code, response.text))
                _ok_reconnect, _res_reconnect = self.restart()
                log.info("Reconnected: {}".format(_ok_reconnect))
                self.add_to_debug_trace("Reconnected: {}".format(_ok_reconnect))
                
                if _ok_reconnect:
                    response = self.call_rest_API("GET", url, headers=headers, params=querystring)
                    log.info("Response: {} - {}".format(response.status_code, response.text))
                    self.add_to_debug_trace("Response: {} - {}".format(response.status_code, response.text))

            if response.status_code == 200:
                _res = response.json()
                log.info("Query result: {}".format(response.text))
                self.add_to_debug_trace("Query result: {}".format(response.text))
                _ok = True

            else:
                raise Exception("Response: {} - {}".format(response.status_code, response.text))

        except Exception as e:
            log.error("Not possible to query iot-broker: {}".format(e))
            self.add_to_debug_trace("Not possible to query iot-broker: {}".format(e))
            _res = e

        return _ok, _res

    # TODO: paginated queries by batch size
    def query_batch(self, ontology, query, query_type, batch_size = None):
        """
        Make a query to iot-broker service of the platform paginated by batch size

        @param ontology     ontology name
        @param query        query expression
        @param query_type   quert type ['NATIVE', 'SQL']
        @param batch_size   batch size (default from configuration)

        @return ok, info
        """
        log.info("Making query batch to ontology:{}, query:{}, query_type:{}".format(ontology, query, query_type))

        _ok = False
        _res = None

        if batch_size is None: 
            batch_size = self.batch_size

        offset = 0
        limit = batch_size

        res_query_count = batch_size
        _res = []
        while res_query_count == batch_size:

            res_query_count = 0
            step_query = self._query_batch_str(query, offset, limit, query_type)
            ok_query, res_query = self.query(ontology, step_query, query_type)
            
            if ok_query: 
                res_query_count = len(res_query)
                _res += res_query
                offset += batch_size

            _ok = ok_query
            
        return _ok, _res

    def _query_batch_str(self, query, offset, limit, query_type):
        step_query = None
        
        if query_type == QueryType.SQL.value:
            step_query = query + " offset {} limit {}".format(offset, limit)
        
        elif query_type == QueryType.NATIVE.value:
            step_query = query + ".skip({}).limit({})".format(offset, limit)
        
        return step_query
        

    # @wait(.1, .1)
    def insert(self, ontology, list_data):
        """
        Make a insert to iot-broker service of the platform

        @param ontology     ontology name
        @param list_data     list with data to insert

        @return ok, info
        """
        _ok = False
        _res = None
        try:
            log.info("Making insert to ontology:{}, elements:{}".format(ontology, len(list_data)))
            url = self.insert_template.substitute(protocol=self.protocol, host=self.host, 
                                                path=self.iot_broker_path, ontology=ontology)
            
            body = json.dumps(list_data)
            body = list_data
            headers = {'Authorization': self.session_key}
            response = self.call_rest_API("POST", url, headers=headers, body=body)
            
            if response.status_code != 200:
                log.info("Session expired, reconnecting...")
                is_reconnected, res_reconnected = self.restart()
                log.info("Reconnected: {}".format(is_reconnected))
                self.add_to_debug_trace("Reconnected: {}".format(is_reconnected))
                
                if is_reconnected:
                    response = self.call_rest_API("POST", url, headers=headers, body=body)

            if response.status_code == 200:
                _res = response.json()
                log.info("Query result: {}".format(response.text))
                self.add_to_debug_trace("Query result: {}".format(response.text))
                _ok = True

            else:
                raise Exception("Response: {} - {}".format(response.status_code, response.text))

        except Exception as e:
            log.error("Not possible to insert with iot-broker: {}".format(e))
            self.add_to_debug_trace("Not possible to insert with iot-broker: {}".format(e))

        return _ok, _res

    # @wait(.1, .1)
    def call_rest_API(self, method, url, headers="", params="", body=""):
        """
        Make an HTTP request

        @param metod   HTTP method ['GET', 'PUT', ...]
        @param url     url path to append to host
        @param header  request headers
        @param params  request params
        @param body    request body

        @return requests.request(...) 
        """
        method = method.upper()
        log.info("Calling rest api, method:{}, url:{}, headers:{}, params:{}"
        .format(method, url, headers, params))
        self.add_to_debug_trace("Calling rest api, method:{}, url:{}, headers:{}, params:{}"
        .format(method, url, headers, params))

        response = requests.request(method, url, headers=headers, params=params, json=body)
        log.info("Call rest api response: {}".format(response))
        self.add_to_debug_trace("Call rest api response: {}".format(response))
        return response
            
    def connect(self):
        return self.join()

    def disconnect(self):
        return self.leave()
    
    def __enter__(self):
        self.join()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.leave()
        return False
