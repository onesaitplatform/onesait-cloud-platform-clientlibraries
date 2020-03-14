import requests
from string import Template
import json
import logging
from onesaitplatform.base import Client
import onesaitplatform.common.config as config
from onesaitplatform.enums import QueryType, RestMethods, RestHeaders
from onesaitplatform.common.log import log

try:
    logging.basicConfig()
    log = logging.getLogger(__name__)
except:
    log.init_logging()

class ModelServiceClient(Client):
    
    # endpoints
    __endpoint_health = "/health"
    __endpoint_information = "/information"
    __endpoint_status = "/status"
    __endpoint_sample = "/sample"
    __endpoint_stats = "/stats"
    __endpoint_predict = "/predict"
    __endpoint_train = "/train"
    
    def __init__(self, host, model_endpoint, model_version, token=None, port=None): #, protocol="http", avoid_ssl_certificate=False):
        Client.__init__(self, host, port=port) # python > 2.7 & <= 3.7.1
        self.__model_endpoint = self.__parse_input_model_endpoint(model_endpoint)
        self.__model_version = self.__parse_input_model_version(model_version)
        self.token = token
        
    def __str__(self):
        """
        String to print object info
        """
        info = "{}(".format(self.__class__.__name__)
        mandatory = "host={host}, model_endpoint={model_endpoint}, model_version={model_version}, ".format(
            host=self.host, model_endpoint=self.model_endpoint, model_version=self.model_version
            )
        
        optionals = "port={port}, protocol={protocol}, avoid_ssl_certificate={avoid_ssl_certificate}, ".format(
            port=self.port, protocol = self.protocol, avoid_ssl_certificate = self.avoid_ssl_certificate
            )
        
        token_str = "token={}".format(self.token)
        if (isinstance(self.token, str)) and (len(self.token) > 50):
            token_str = "token={}".format(self.token[:20] + "..." + self.token[-20:])
        
        info += mandatory + optionals + token_str + ")"
        
        return info
    
    @property
    def model_endpoint(self):
        return self.__model_endpoint
    
    def __parse_input_model_endpoint(self, model_endpoint):
        if not model_endpoint.startswith("/"):
            model_endpoint = "/{}".format(model_endpoint)
        return model_endpoint
    
    @property
    def model_version(self):
        return self.__model_version
    
    def __parse_input_model_version(self, model_version):
        model_version = str(model_version)
        if model_version.startswith("v"):
            model_version = model_version[1:]
        return model_version
    
    @property
    def model_version_str(self):
        return self.model_version if self.model_version.startswith("v") else "v{}".format(self.model_version)
    
    @property
    def is_connected(self):
        is_healthly = False
        try:
            is_healthly = self.health().status_code == 200
        except Exception as e:
            log.warning("Not possible to connect with remote model: {}".format(e))
        finally:
            self.__is_connected = is_healthly
        return self.__is_connected
    
    @is_connected.setter
    def is_connected(self, is_con):
        self.__is_connected = is_con
    
    @property
    def suported_protocols(self):
        return self.__supported_protocols
    
    @property
    def model_path(self):
        return "{}/api/{}".format(self.model_endpoint, self.model_version_str)
    
    @property
    def model_url(self):
        url = self.host
        if self.protocol:
            url = "{}://{}".format(self.protocol, self.host)
        
        if self.port:
            url += ":{}".format(self.port)
        url += self.model_path
        
        return url

    def __set_token_if_not_none(self, token):
        if token:
            self.token = token
    
    @property
    def __headers(self):
        return {
            RestHeaders.AUTHORIZATION.value: self.token,
            RestHeaders.CONT_TYPE.value: RestHeaders.APP_JSON.value
        }
    
    def health(self, token=None):
        self.__set_token_if_not_none(token)
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_health)        
        return self.call(RestMethods.GET.value, url, headers=self.__headers)
    
    def status(self, token=None):
        self.__set_token_if_not_none(token)
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_status)
        return self.call(RestMethods.GET.value, url, headers=self.__headers)
    
    def information(self, token=None):
        self.__set_token_if_not_none(token)
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_information)
        return self.call(RestMethods.GET.value, url, headers=self.__headers)
    
    def sample(self, token=None):
        self.__set_token_if_not_none(token)
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_sample)
        return self.call(RestMethods.GET.value, url, headers=self.__headers)
    
    def stats(self, token=None):
        self.__set_token_if_not_none(token)
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_stats)
        return self.call(RestMethods.GET.value, url, headers=self.__headers)
    
    def predict(self, data, token=None):
        self.__set_token_if_not_none(token)
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_predict)
        """if not isinstance(data, str):
            data = json.dumps(data)"""
        #return requests.post(url, headers=self.__headers, data=data, proxies=self.proxies)
        return self.call(RestMethods.POST.value, url, headers=self.__headers, body=data)
    
    def train(self, data, token=None):
        self.__set_token_if_not_none(token)
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_train)
        """if not isinstance(data, str):
            data = json.dumps(data)"""
        #return requests.post(url, headers=self.__headers, data=data, proxies=self.proxies)
        return self.call(RestMethods.POST.value, url, headers=self.__headers, body=data)
    
    def to_json(self, as_string=False):
        """
        Export object to json

        @param as_string    If json dumped (String)

        @return json_obj   json-dict/ json string
        """
        json_obj = dict()
        json_obj["host"] = self.host
        json_obj["port"] = self.port
        json_obj["protocol"] = self.protocol
        json_obj["is_connected"] = self.is_connected
        json_obj["proxies"] = self.proxies
        json_obj["timeout"] = self.timeout
        json_obj["avoid_ssl_certificate"] = self.avoid_ssl_certificate
        json_obj["raise_exceptions"] = self.raise_exceptions
        json_obj["model_endpoint"] = self.model_endpoint
        json_obj["model_version"] = self.model_version
        json_obj["token"] = self.token
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

            client = Client(host=json_object['host'])
            if "port" in json_object_keys:
                client.port = json_object['port']
            if "protocol" in json_object_keys:
                client.protocol = json_object['protocol']
            if "is_connected" in json_object_keys:
                client.is_connected = json_object['is_connected'] 
            if "proxies" in json_object_keys:
                client.proxies = json_object['proxies']
            if "timeout" in json_object_keys:
                client.timeout = json_object['timeout']
            if "avoid_ssl_certificate" in json_object_keys:
                client.avoid_ssl_certificate = json_object['avoid_ssl_certificate']
            if "raise_exceptions" in json_object_keys:
                client.raise_exceptions = json_object['raise_exceptions']
            if "model_endpoint" in json_object_keys:
                client.model_endpoint = json_object['model_endpoint']
            if "model_version" in json_object_keys:
                client.model_version = json_object['model_version']
            if "token" in json_object_keys:
                client.token = json_object['token']
                
            log.info("Imported json {}".format(json_object))
            client.add_to_debug_trace("Imported json {}"
                                          .format(json_object))

        except Exception as e:
            log.error("Not possible to import object from json: {}".format(e))

        return client
    