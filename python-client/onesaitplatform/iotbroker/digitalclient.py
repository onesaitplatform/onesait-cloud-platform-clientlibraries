import requests
from string import Template
import json
import logging
from onesaitplatform.base import Client
from onesaitplatform.common.utils import wait
import onesaitplatform.common.config as config
from onesaitplatform.enums import RestMethods, QueryType, RestHeaders
from onesaitplatform.common.log import log

try:
    logging.basicConfig()
    log = logging.getLogger(__name__)
except:
    log.init_logging()


class DigitalClient(Client):
    """
    Class DigitalClient to connect with Iot-Broker of OnesaitPlatform
    """
    __iot_broker_path = config.IOT_BROKER_PATH
    batch_size = config.BATCH_QUERY_SIZE

    __join_template = Template("$protocol://$host$path/rest/client/join")
    __leave_template = Template("$protocol://$host$path/rest/client/leave")
    __query_template = Template("$protocol://$host$path/rest/ontology/$ontology")
    __insert_template = Template("$protocol://$host$path/rest/ontology/$ontology")
    __update_template = Template("$protocol://$host$path/rest/ontology/$ontology/update")
    __delete_template = Template("$protocol://$host$path/rest/ontology/$ontology/$entity")
    

    __query_batch_sql = Template("$query offset $start limit $end")
    __query_batch_mongo = Template("$query.skip($start).limit($end)")

    def __init__(self, host, port=None,
                 iot_client=config.IOT_CLIENT,
                 iot_client_token=config.IOT_CLIENT_TOKEN):
        """
        Class IotBrokerClient to connect with Iot-Broker of OnesaitPlatform

        @param host               Onesaitplatform host
        @param iot_client         Onesaitplatform Iot-Client
        @param iot_client_token   Onesaitplatform iot-Client-Token
        """
        #super().__init__(host, port=port) # only python > 3
        Client.__init__(self, host, port=port) # python > 2.7 & <= 3.7.1
        self.iot_client = iot_client
        self.iot_clientId = iot_client + ":PythonClient"
        self.iot_client_token = iot_client_token
        self.session_key = None

        log.info("Created client with iot-broker \
                 host:{}, path:{}, client:{}, token:{}"
                 .format(host, self.__iot_broker_path,
                         iot_client, iot_client_token))
        self.add_to_debug_trace("Created client with \
                                iot-broker host:{}, path:{}, \
                                client:{}, token:{}"
                                .format(host, self.__iot_broker_path,
                                        iot_client, iot_client_token))

    def __str__(self):
        """
        String to print object info
        """
        hide_attributes = ["debug_trace"]
        info = "{}".format(self.__class__.__name__)
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
        json_obj = dict()
        json_obj["host"] = self.host
        json_obj["port"] = self.port
        json_obj["protocol"] = self.protocol
        json_obj["iot_client"] = self.iot_client
        json_obj["iot_client_token"] = self.iot_client_token
        json_obj["is_connected"] = self.is_connected
        json_obj["session_key"] = self.session_key        
        json_obj["proxies"] = self.proxies
        json_obj["timeout"] = self.timeout
        json_obj["avoid_ssl_certificate"] = self.avoid_ssl_certificate
        json_obj["raise_exceptions"] = self.raise_exceptions

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

        @return client        client object
        """
        client = None
        try:
            if type(json_object) == str:
                json_object = json.loads(json_object)

            json_object_keys = list(json_object.keys())
            client = DigitalClient(host=json_object['host'])
            if "port" in json_object_keys:
                client.port = json_object['port']
            if "iot_client" in json_object_keys:
                client.iot_client = json_object['iot_client']
            if "iot_client_token" in json_object_keys:
                client.iot_client_token = json_object['iot_client_token'] 
            if "is_connected" in json_object_keys:
                client.is_connected = json_object['is_connected'] 
            if "session_key" in json_object_keys:
                client.session_key = json_object['session_key']
            if "protocol" in json_object_keys:
                client.protocol = json_object['protocol']
            if "proxies" in json_object_keys:
                client.proxies = json_object['proxies']
            if "timeout" in json_object_keys:
                client.timeout = json_object['timeout']
            if "avoid_ssl_certificate" in json_object_keys:
                client.avoid_ssl_certificate = json_object['avoid_ssl_certificate']
            if "raise_exceptions" in json_object_keys:
                client.raise_exceptions = json_object['raise_exceptions']

            log.info("Imported json {}".format(json_object))
            client.add_to_debug_trace("Imported json {}".format(json_object))

        except Exception as e:
            log.error("Not possible to import object from json: {}".format(e))

        return client

    def raw_join(self, iot_client=None, iot_client_token=None):
        """
        Login in the platform with Iot-Client credentials

        @return response
        """

        if iot_client is not None:
            self.iot_client = iot_client
        if iot_client_token is not None:
            self.iot_client_token = iot_client_token

        log.info("Created connection with iot-broker host:{}, path:{}, client:{}, token:{}"
                     .format(self.host, self.__iot_broker_path, self.iot_client, self.iot_client_token))

        url = self.__join_template.substitute(protocol=self.protocol, host=self.hostport, path=self.__iot_broker_path)
        querystring = {"token": self.iot_client_token, "clientPlatform": self.iot_client, "clientPlatformId": self.iot_clientId}
        headers = {
            RestHeaders.ACCEPT_STR.value: RestHeaders.APP_JSON.value,
            RestHeaders.CONT_TYPE.value: RestHeaders.APP_JSON.value
            }
        response = self.call(RestMethods.GET.value, url, headers, querystring)

        if response.status_code == 200:
            self.session_key = response.json()["sessionKey"]
            self.is_connected = True
            log.info("Logged correctly with session_key: {}".format(response.text))
            self.add_to_debug_trace("Logged correctly with session_key: {}".format(response.text))

        else:
            log.info("Not possible to loggin: {} - {}".format(response.status_code, response.text))
            self.add_to_debug_trace("Not possible to loggin: {} - {}".format(response.status_code, response.text))

        return response

    def join(self, iot_client=None, iot_client_token=None):
        """
        Login in the platform with Iot-Client credentials

        @return ok, info
        """
        _ok = False
        _res = None

        try:
            response = self.raw_join(iot_client=iot_client, iot_client_token=iot_client_token)

            if response.status_code == 200:
                _res = response.json()
                _ok = True

            else:
                _res = response.text

        except Exception as e:
            self.is_connected = False
            log.error("Not possible to conect with iot-broker: {}".format(e))
            self.raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

    def raw_leave(self):
        """
        Logout in the platform with session token

        @return response
        """
        log.info("Leaving connection with session_key:{}".format(self.session_key))
        if self.is_connected:
            url = self.__leave_template.substitute(protocol=self.protocol, host=self.hostport, path=self.__iot_broker_path)
            headers = {RestHeaders.AUTHORIZATION.value: self.session_key}
            response = self.call(RestMethods.GET.value, url, headers=headers)
            self.is_connected = False
            self.session_key = None
            log.info("Disconnected correctly: {}".format(response.text))
            self.add_to_debug_trace("Disconnected correctly: {}".format(response.text))
        else:
            log.info("There is not connection, please join() before leave()")
            self.add_to_debug_trace("There is not connection, please join() before leave()")

        return response

    def leave(self):
        """
        Logout in the platform with session token

        @return ok, info
        """
        _ok = False
        _res = None
        try:
            if self.is_connected:
                response = self.raw_leave()
                if response.status_code == 200:
                    _res = response.text
                    _ok = True
            else:
                _ok = True

        except Exception as e:
            log.error("Not possible to disconnect with iot-broker: {}".format(e))
            self.add_to_debug_trace("Not possible to disconnect with iot-broker: {}".format(e))
            self.raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

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

    def raw_query(self, ontology, query, query_type):
        """
        Make a query to iot-broker service of the platform

        @param ontology     ontology name
        @param query        query expression
        @param query_type   quert type ['NATIVE', 'SQL']

        @return  response
        """
        assert ontology != None, "Invalid input ontology"
        assert query != None, "Invalid input query"
        assert query_type != None, "Invalid input query_type"

        log.info("Making query to ontology:{}, query:{}, query_type:{}".format(ontology, query, query_type))
        url = self.__query_template.substitute(protocol=self.protocol, host=self.hostport,
                                                path=self.__iot_broker_path, ontology=ontology)
        querystring = {"query": query, "queryType": query_type.upper()}
        headers = {RestHeaders.AUTHORIZATION.value: self.session_key}
        response = self.call(RestMethods.GET.value, url, headers=headers, params=querystring)

        if response.status_code != 200:
            log.warn("Response: {status_code} - {text}".format(status_code=response.status_code, text=response.text))
            self.add_to_debug_trace("Response: {status_code} - {text}".format(status_code=response.status_code, text=response.text))
            log.info("Not possible to connect ({}) - {}, reconnecting...".format(response.status_code, response.text))
            _ok_reconnect, _res_reconnect = self.restart()
            log.info("Reconnected: {} - {}".format(_ok_reconnect, _res_reconnect))
            self.add_to_debug_trace("Reconnected: {} - {}".format(_ok_reconnect, _res_reconnect))

            if _ok_reconnect:
                headers = {RestHeaders.AUTHORIZATION.value: self.session_key}
                response = self.call(RestMethods.GET.value, url, headers=headers, params=querystring)

        return response

    def query(self, ontology, query, query_type):
        """
        Make a query to iot-broker service of the platform

        @param ontology     ontology name
        @param query        query expression
        @param query_type   quert type ['NATIVE', 'SQL']

        @return ok, info
        """
        _ok = False
        _res = None
        try:
            response = self.raw_query(ontology, query, query_type)

            if self.is_correct_status_code(response.status_code):
                _res = response.json()
                log.info("Response: {} - {}".format(response.status_code, _res[0] if len(_res) > 0 else []))
                self.add_to_debug_trace("Response: {} - {}".format(response.status_code, _res[0] if len(_res) > 0 else []))
                _ok = True

            else:
                log.info("Bad response: {status_code} - {text}".format(status_code=response.status_code, text=response.text))
                self.add_to_debug_trace("Bad response: {status_code} - {text}".format(status_code=response.status_code, text=response.text))
                _res = response.text

        except Exception as e:
            log.error("Not possible to query iot-broker: {}".format(e))
            self.add_to_debug_trace("Not possible to query iot-broker: {}".format(e))
            self.raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

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

    def raw_insert(self, ontology, list_data):
        """
        Make a insert to iot-broker service of the platform

        @param ontology     ontology name
        @param list_data     list with data to insert

        @return response
        """
        assert ontology != None, "Invalid input ontology"
        assert list_data != None, "Invalid input list_data"

        if isinstance(list_data, str):
            list_data = json.loads(list_data)

        log.info("Making insert to ontology:{}, elements:{}".format(ontology, len(list_data)))

        url = self.__insert_template.substitute(protocol=self.protocol, host=self.hostport,
                                            path=self.__iot_broker_path, ontology=ontology)
        body = list_data
        headers = {RestHeaders.AUTHORIZATION.value: self.session_key}
        response = self.call(RestMethods.POST.value, url, headers=headers, body=body)

        if response.status_code != 200:
            log.info("Session expired, reconnecting...")
            is_reconnected, _ = self.restart()
            log.info("Reconnected: {}".format(is_reconnected))
            self.add_to_debug_trace("Reconnected: {}".format(is_reconnected))

            if is_reconnected:
                headers = {RestHeaders.AUTHORIZATION.value: self.session_key}
                response = self.call(RestMethods.POST.value, url, headers=headers, body=body)

        return response

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
            response = self.raw_insert(ontology, list_data)

            if self.is_correct_status_code(response.status_code):
                _res = response.json()
                log.info("Query result: {}".format(response.text))
                self.add_to_debug_trace("Query result: {}".format(response.text))
                _ok = True

            else:
                log.info("Bad response: {} - {}".format(response.status_code, response.text))
                self.add_to_debug_trace("Bad response: {} - {}".format(response.status_code, response.text))
                _res = response.text

        except Exception as e:
            log.error("Not possible to insert with iot-broker: {}".format(e))
            self.add_to_debug_trace("Not possible to insert with iot-broker: {}".format(e))
            self.raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

    def raw_update(self, ontology, query=None, query_type="NATIVE", data=None, where=None, return_ids=True):
        """
        Make a update to iot-broker service of the platform

        @param ontology     ontology name
        @param query        query expression
        @param query_type   query type ['NATIVE']
        @param data         Optional - data to update, only if not using query
        @param where        OptionaL - selection criteria for the update, only if not using query
        @param return_ids   Optional - return ids in response (default: True)

        @return response
        """
        assert ontology is not None, "Invalid input ontology"

        if return_ids: return_ids = "true"
        else: return_ids = "false"

        if query is None:
            assert data is not None, "Invalid data"
            assert isinstance(data, dict), "Invalid data type"
            if where is None:
                where = {}
            log.info("Making update to ontology:{}, elements:{}".format(ontology, data))

        if data is None:
            assert query is not None, "Invalid query"
            log.info("Making query update to ontology:{}, query:{}, query_type:{}".format(ontology, query, query_type))

        url = self.__update_template.substitute(protocol=self.protocol, host=self.hostport,
                                            path=self.__iot_broker_path, ontology=ontology)

        body = query

        if not query:
            parameters = {"multi": "true"}  # It could be added as a parameter on a future release
            body = "db.{}.update({},{},{})".format(ontology, where, data, parameters)

        params = {"ids": return_ids}
        headers = {RestHeaders.AUTHORIZATION.value: self.session_key}

        response = self.call(RestMethods.PUT.value, url, headers=headers, params=params, body=body)

        if response.status_code != 200:
            log.info("Session expired, reconnecting...")
            is_reconnected, _ = self.restart()
            log.info("Reconnected: {}".format(is_reconnected))
            self.add_to_debug_trace("Reconnected: {}".format(is_reconnected))

            if is_reconnected:
                headers = {RestHeaders.AUTHORIZATION.value: self.session_key}
                response = self.call(RestMethods.PUT.value, url, headers=headers, body=body, params={"ids": True})

        return response

    def update(self, ontology, query=None, query_type="NATIVE", data=None, where=None, return_ids=True):
        """
        Make a update to iot-broker service of the platform

        @param ontology     ontology name
        @param query        query expression
        @param query_type   quert type ['NATIVE']
        @param data         data to update
        @param where        selection criteria for the update
        @param return_ids   return ids in response (optional, default: True)

        @return ok, info
        """
        _ok = False
        _res = None
        try:
            response = self.raw_update(ontology, query, query_type, data, where, return_ids)

            if self.is_correct_status_code(response.status_code):
                _res = response.json()
                log.info("Query result: {}".format(response.text))
                self.add_to_debug_trace("Query result: {}".format(response.text))
                _ok = True

            else:
                log.info("Bad response: {} - {}".format(response.status_code, response.text))
                self.add_to_debug_trace("Bad response: {} - {}".format(response.status_code, response.text))
                _res = response.text

        except Exception as e:
            log.error("Not possible to update with iot-broker: {}".format(e))
            self.add_to_debug_trace("Not possible to update with iot-broker: {}".format(e))
            self.raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

    def raw_delete(self, ontology, entity_id, return_ids=True):
        """
        Make a delete to iot-broker service of the platform

        @param ontology     ontology name
        @param entity_id    entity object id
        @param return_ids   return ids in response (optional, default: True)

        @return response
        """
        assert ontology != None, "Invalid input ontology"
        assert entity_id != None, "Invalid input entity_id"

        if return_ids: return_ids = "true"
        else: return_ids = "false"

        log.info("Making delete to ontology:{}, entity_id:{}".format(ontology, entity_id))

        url = self.__delete_template.substitute(protocol=self.protocol, host=self.hostport,
                                            path=self.__iot_broker_path, ontology=ontology,
                                            entity=entity_id)
        params = {"ids": return_ids}
        headers = {RestHeaders.AUTHORIZATION.value: self.session_key}
        response = self.call(RestMethods.DELETE.value, url, headers=headers, params=params)

        if response.status_code != 200:
            log.info("Session expired, reconnecting...")
            is_reconnected, _ = self.restart()
            log.info("Reconnected: {}".format(is_reconnected))
            self.add_to_debug_trace("Reconnected: {}".format(is_reconnected))

            if is_reconnected:
                headers = {RestHeaders.AUTHORIZATION.value: self.session_key}
                response = self.call(RestMethods.DELETE.value, url, headers=headers, params=params)

        return response

    def delete(self, ontology, entity_id, return_ids=True):
        """
        Make a insert to iot-broker service of the platform

        @param ontology     ontology name
        @param entity_id    entity object id
        @param return_ids   return ids in response (optional, default: True)

        @return ok, info
        """
        _ok = False
        _res = None
        try:
            response = self.raw_delete(ontology, entity_id, return_ids)

            if self.is_correct_status_code(response.status_code):
                _res = response.json()
                log.info("Query result: {}".format(response.text))
                self.add_to_debug_trace("Query result: {}".format(response.text))
                _ok = True

            else:
                log.info("Bad response: {} - {}".format(response.status_code, response.text))
                self.add_to_debug_trace("Bad response: {} - {}".format(response.status_code, response.text))
                _res = response.text

        except Exception as e:
            log.error("Not possible to delete with iot-broker: {}".format(e))
            self.add_to_debug_trace("Not possible to delete with iot-broker: {}".format(e))
            self.raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

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
