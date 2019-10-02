import requests
from string import Template
import json
import logging
try:
    from onesaitplatform.base import Client
    import onesaitplatform.common.config as config
    from onesaitplatform.enum import QueryType
    from onesaitplatform.enum import RestMethods
    from onesaitplatform.enum import RestHeaders
    from onesaitplatform.common.log import log
except Exception as e:
    print("Error - Not possible to import necesary libraries: {}".format(e))

try:
    logging.basicConfig()
    log = logging.getLogger(__name__)
except:
    log.init_logging()

class ModelServiceClient:
    
    # connection
    __supported_protocols = ["http", "https"]
    
    # endpoints
    __endpoint_health = "/health"
    __endpoint_information = "/information"
    __endpoint_status = "/status"
    __endpoint_sample = "/sample"
    __endpoint_stats = "/stats"
    __endpoint_predict = "/predict"
    __endpoint_train = "/train"
    
    def __init__(self, host, model_endpoint, model_version, token=None, port=None, protocol="http", avoid_ssl_certificate=False):
        self.__host = host
        self.__model_endpoint = model_endpoint
        self.__model_version = str(model_version)
        self.token = token
        self.__port = port
        self.__protocol = protocol
        self.__avoid_ssl_certificate = avoid_ssl_certificate
        
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
    def host(self):
        return self.__host
    
    @property
    def model_endpoint(self):
        return self.__model_endpoint
    
    @property
    def model_version(self):
        return self.__model_version
    
    @property
    def model_version_str(self):
        return self.model_version if self.model_version.startswith("v") else "v{}".format(self.model_version)
    
    @property
    def port(self):
        return self.__port
    
    @property
    def protocol(self):
        return self.__protocol
    
    @protocol.setter
    def protocol(self, p):
        if not isinstance(p, str):
            raise ValueError("Not supported protocol value type (only str)")
        else:
            p = p.lower()
            
        if not p in self.suported_protocols:
            print("WARNING - protocol {} not in supported protocols {}, using 'http' default".format(p, self.suported_protocols))
            p = "http"
        self.__protocol = p 
    
    @property
    def suported_protocols(self):
        return self.__supported_protocols
        
    @property
    def avoid_ssl_certificate(self): # not necessary
        return self.__avoid_ssl_certificate
    
    @avoid_ssl_certificate.setter # not necessary
    def avoid_ssl_certificate(self, avoid):
        if self.protocol == "http":
            self.__avoid_ssl_certificate = False
        else:
            self.__avoid_ssl_certificate = bool(avoid)
        
    @property
    def model_path(self):
        return "/{}/api/{}".format(self.model_endpoint, self.model_version_str)
    
    @property
    def model_url(self):
        url = self.host
        if self.protocol:
            url = "{}://{}".format(self.protocol, self.host)
        
        if self.port:
            url += ":{}".format(self.port)
        url += self.model_path
        
        return url
    
    @property
    def __headers(self):
        return {
            "Authorization": self.token,
            "Content-Type": "application/json"
        }
    
    def health(self, token=None):
        if token:
            self.token = token
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_health)
        print("making request to url {}".format(url))
        return requests.get(url, headers=self.__headers)
    
    def status(self, token=None):
        if token:
            self.token = token
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_status)
        print("making request to url {}".format(url))
        return requests.get(url, headers=self.__headers)
    
    def information(self, token=None):
        if token:
            self.token = token
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_information)
        print("making request to url {}".format(url))
        return requests.get(url, headers=self.__headers)
    
    def sample(self, token=None):
        if token:
            self.token = token
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_sample)
        print("making request to url {}".format(url))
        return requests.get(url, headers=self.__headers)
    
    def stats(self, token=None):
        if token:
            self.token = token
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_stats)
        print("making request to url {}".format(url))
        return requests.get(url, headers=self.__headers)
    
    def predict(self, data, token=None):
        if token:
            self.token = token
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_predict)
        print("making request to url {}".format(url))
        if not isinstance(data, str):
            data = json.dumps(data)
        return requests.post(url, headers=self.__headers, data=data)
    
    def train(self, data, token=None):
        if token:
            self.token = token
        url = "{url}{op_endpoint}".format(url=self.model_url, op_endpoint=self.__endpoint_train)
        print("making request to url {}".format(url))
        if not isinstance(data, str):
            data = json.dumps(data)
        return requests.post(url, headers=self.__headers, data=data)
    