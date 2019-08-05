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


class ApiManagerClient(Client):
    """
    Class ApiManagerClient to connect with Api-Manager and APIs of OnesaitPlatform
    """
    api_manager_path = config.API_MANAGER_PATH
    api_caller_path = config.API_CALLER_PATH

    __find_template = Template("$protocol://$host$path/apis?identificacion=$identification&estado=$state&usuario=$user")
    __create_template = Template("$protocol://$host$path/apis")
    __delete_template = Template("$protocol://$host$path/apis/$identification/$version")
    __call_template = Template("$protocol://$host$path/$url")
    __list_template = Template("$protocol://$host$path//apis/user/$user")
    __request_template = Template("$protocol://$host$path$api_path")
    
    __IS_LIST_QUERY_STR = "/apis/user"
    __IS_FIND_QUERY_STR = "/apis?"
    
    NOT_SETTED_TOKEN_MSG = "Note token setted. Please use setToken(<token>) before"
    NOT_POSSIBLE_CONNECT_MSG = "Not possible to connect"

    def __init__(self, host, port=None):
        """
        Class ApiManagerClient to connect with Api-Manager and APIs of OnesaitPlatform

        @param host               Onesaitplatform host
        """
        #super().__init__(host, port=port) # only python > 3
        Client.__init__(self, host, port=port) # python > 2.7 & <= 3.7.1
        self.token = None
        log.info("Connection Params: "  + self.host + "/(" + self.api_manager_path + " - " + self.api_caller_path + ")")
        self.add_to_debug_trace("Connection Params: "  + self.host + "/(" + self.api_manager_path + " - " + self.api_caller_path + ")")
    
    def setToken(self, token):
        """ Set token (== object.token = <token>)"""
        self.token = token
    
    @property
    def __headers(self):
        return {
            RestHeaders.X_OP_APIKey.value: self.token,
            RestHeaders.ACCEPT_STR.value: RestHeaders.APP_JSON.value,
            RestHeaders.CONT_TYPE.value: RestHeaders.APP_JSON.value,
            RestHeaders.USER_AGENT.value: self.user_agent
        }
    
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
            connection = ApiManagerClient(host=json_object['host'])
            connection.is_connected = json_object['is_connected']
            log.info("Imported json {}".format(json_object))
            connection.add_to_debug_trace("Imported json {}".format(json_object))

        except Exception as e:
            log.error("Not possible to import object from json: {}".format(e))
        
        return connection

    def raise_exception_if_not_token(self):
        if self.token is None:
            raise Exception(self.NOT_SETTED_TOKEN_MSG)

    def find(self, identification, state, user):
        """
        Find API rest information

        @param identification     API identification (name)
        @param state              API state
        @param user               API creation user

        @return ok, info  
        """
        _ok = False
        _res = None

        try:
            log.info("Making find: identification:{}, state:{}, user:{}".format(identification, state, user))
            self.raise_exception_if_not_token()
            url = self.__find_template.substitute(protocol=self.protocol, host=self.hostport, 
                                                path=self.api_manager_path, 
                                                identification=identification,
                                                state=state, 
                                                user=user
                                                )
            headers = self.__headers
            response = self.call(RestMethods.GET.value, url, headers=headers)
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
            log.error("Not possible to query api-manager: {}".format(e))
            self.add_to_debug_trace("Not possible to query api-manager: {}".format(e))
            _res = e

        return _ok, _res

    def create(self, json_obj):
        """
        Create an API rest from json object

        @param json_obj     json object of the API

        @return ok, info  
        """
        _ok = False
        _res = None

        try:
            log.info("Making create")
            if isinstance(json_obj, str):
                json_obj = json.loads(json_obj)

            self.raise_exception_if_not_token()
            url = self.__create_template.substitute(protocol=self.protocol, host=self.hostport, 
                                                path=self.api_manager_path
                                                )
            headers = self.__headers
            response = self.call(RestMethods.POST.value, url, headers=headers, body=json_obj)
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
            log.error("Not possible to query api-manager: {}".format(e))
            self.add_to_debug_trace("Not possible to query api-manager: {}".format(e))
            _res = e

        return _ok, _res
    
    def delete(self, identification, version):
        """
        Delete an API rest

        @param identification     API identification (name)
        @param version            API version

        @return ok, info  
        """
        _ok = False
        _res = None

        try:
            log.info("Making delete")
            if isinstance(version, str):
                version = version.replace("v", "") # case vX -> X

            self.raise_exception_if_not_token()
            url = self.__delete_template.substitute(protocol=self.protocol, host=self.hostport, 
                                                path=self.api_manager_path,
                                                identification=identification,
                                                version=version
                                                )
            headers = self.__headers
            response = self.call(RestMethods.DELETE.value, url, headers=headers)
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
            log.error("Not possible to query api-manager: {}".format(e))
            self.add_to_debug_trace("Not possible to query api-manager: {}".format(e))
            _res = e

        return _ok, _res

    def generate_url_request(self, name, version, path_params, query_params):
        url = ""
    	# version
        if version.startswith("/"):
    	    url = version
            
        elif version.lower().startswith("v"):
            url = "/" + version

        else:
            url = "/v" + version

        if name.startswith("/"):
            url += name
    	
        else:
            url = url + "/" + name
    		
    	# path params
        if path_params is not None:
            for param in path_params:
                url = url + "/" + param
	    	
    	# query params
        if query_params is not None:
            url += "?"
            for key, value in query_params.items():
                url = url + "&" + key + "=" + value

        return url

    def request(self, method, url=None, name=None, version=None, 
                path_params=None, query_params=None, body=None):
        """
        Make a request to an API rest

        @param method          method (hhtp method)
        @param url             url with path params and query params
        @param name            API rest name *
        @param version         API rest version *
        @param path_params     path_params *
        @param query_params    query_params *
        @param body            body

        * If url != None: api_path = generate_url_request(name, version, path_params, query_params)
        * else: api_path = url 

        @return ok, info  
        """
        _ok = False
        _res = None

        try:
            log.info("Making request: url:{}".format(url))
            self.raise_exception_if_not_token()

            if isinstance(version, int) or isinstance(version, float):
                version = str(int(version))
            
            if url is None:
                api_path = self.generate_url_request(name=name, version=version, 
                                                     path_params=path_params, query_params=query_params)
            else:
                api_path = url
            
            url = self.__request_template.substitute(protocol=self.protocol, host=self.hostport, 
                                                path=self.api_caller_path,
                                                api_path=api_path
                                                )
            headers = self.__headers
            response = self.call(method, url, headers=headers, body=body)
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
            log.error("Not possible to query api-manager: {}".format(e))
            self.add_to_debug_trace("Not possible to query api-manager: {}".format(e))
            _res = e

        return _ok, _res
    
    def list(self, user):
        """
        List APIs rest from user

        @param user     user

        @return ok, info  
        """
        _ok = False
        _res = None

        try:
            log.info("Making list: user: {}".format(user))
            self.raise_exception_if_not_token()
            url = self.__list_template.substitute(protocol=self.protocol, host=self.hostport, 
                                                path=self.api_manager_path,
                                                user=user
                                                )
            headers = self.__headers
            response = self.call(RestMethods.GET.value, url, headers=headers)
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
            log.error("Not possible to query api-manager: {}".format(e))
            self.add_to_debug_trace("Not possible to query api-manager: {}".format(e))
            _res = e

        return _ok, _res

    def call(self, method, url, headers=None, params=None, body=None):
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

        response = requests.request(method, url, headers=headers, params=params, json=body, verify=not self.avoid_ssl_certificate,
                                    timeout=self.timeout, proxies=self.proxies)
        log.info("Call rest api response: {}".format(response))
        self.add_to_debug_trace("Call rest api response: {}".format(response))

        return response

    def __is_list_query(self, url):
        return self.__IS_LIST_QUERY_STR in url
    
    def __is_find_query(self, url):
        return self.__IS_FIND_QUERY_STR in url
