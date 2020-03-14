import requests
from string import Template
import json
import logging
from .token import Token
from onesaitplatform.base import Client
import onesaitplatform.common.config as config
from onesaitplatform.enums import RestMethods, QueryType, RestHeaders
from onesaitplatform.common.log import log

try:
    logging.basicConfig()
    log = logging.getLogger(__name__)
except:
    log.init_logging()


class AuthClient(Client):
    """
    Class AuthClient to login and refresh token of OnesaitPlatform
    """
    __control_panel_path = config.CONTROL_PANEL_PATH

    __login_template = Template("$protocol://$host$path/api/login")
    __info_template = Template("$protocol://$host$path/api/login/info")
    __refresh_template = Template("$protocol://$host$path/api/login/refresh_token")

    def __init__(self, host, port=None, username=None, password=None, vertical="onesaitplatform"):
        """
        Class AuthClient to login and refresh token of OnesaitPlatform

        @param host               Onesaitplatform host
        @param port               Onesaitplatform port
        @param username           Onesaitplatform username
        @param password           Onesaitplatform password
        @param vertical           Onesaitplatform vertical (default=''onesaitplatform')
        """
        Client.__init__(self, host, port=port)
        self.username = username
        self.password = password
        self.vertical = vertical
        self.token = None

        log.info("Created client with controlpanel-auth \
                 host:{}, path:{}, username:{}, vertical:{}"
                 .format(host, self.__control_panel_path,
                         username, vertical))
        self.add_to_debug_trace("Created client with controlpanel-auth \
                 host:{}, path:{}, username:{}, vertical:{}"
                 .format(host, self.__control_panel_path,
                         username, vertical))

    def __str__(self):
        """
        String to print object info
        """
        hide_attributes = ["debug_trace", "password"]
        info = "{}".format(self.__class__.__name__)
        info += "("
        for k, v in self.__dict__.items():
            if k not in hide_attributes:
                info += "{}={}, ".format(k, v)
        info = info[:-2] + ")"

        return info

    @property
    def token_str(self):
        res = None
        if isinstance(self.token, Token):
            res = self.token.access_token_str
        return res

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
        json_obj["username"] = self.username
        json_obj["password"] = self.password
        json_obj["vertical"] = self.vertical
        json_obj["token"] = self.token.to_json()
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
            client = AuthClient(host=json_object['host'])
            if "port" in json_object_keys:
                client.port = json_object['port']
            if "username" in json_object_keys:
                client.username = json_object['username']
            if "password" in json_object_keys:
                client.password = json_object['password'] 
            if "vertical" in json_object_keys:
                client.vertical = json_object['vertical'] 
            if "token" in json_object_keys:
                client.token = Token.from_json(json_object['token'])
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

    def raw_login(self, username=None, password=None, vertical=None):
        """
        Login in the platform with User credentials

        @return response
        """

        if username is not None:
            self.username = username
        if password is not None:
            self.password = password
        if vertical is not None:
            self.vertical = vertical

        log.info("Created connection with controlpanel-auth host:{}, path:{}, username:{}, vertical:{}"
                     .format(self.host, self.__control_panel_path, self.username, self.vertical))

        url = self.__login_template.substitute(
            protocol=self.protocol, host=self.hostport, path=self.__control_panel_path)
        body = {
            "username": self.username, "password": self.password, "vertical": self.vertical}
        headers = {
            RestHeaders.ACCEPT_STR.value: RestHeaders.APP_JSON.value,
            RestHeaders.CONT_TYPE.value: RestHeaders.APP_JSON.value
            }
        response = self.call(RestMethods.POST.value, url, headers, body=body)

        if response.status_code == 200:
            self.token = Token.from_json(response.json())
            log.info("Logged correctly, access token: {}".format(self.token))
            self.add_to_debug_trace("Logged correctly, access token: {}".format(self.token))

        else:
            log.info("Not possible to login: {} - {}".format(response.status_code, response.text))
            self.add_to_debug_trace("Not possible to login: {} - {}".format(response.status_code, response.text))

        return response

    def login(self, username=None, password=None, vertical=None):
        """
        Login in the platform with Iot-Client credentials

        @return ok, info
        """
        _ok = False
        _res = None

        try:
            response = self.raw_login(username=username, password=password, vertical=vertical)

            if response.status_code == 200:
                _res = response.json()
                _ok = True

            else:
                _res = response.text

        except Exception as e:
            log.error("Not possible to login with controlpanel-auth host:{}, path:{}, username:{}, vertical:{}"
                     .format(self.host, self.__control_panel_path, self.username, self.vertical))
            self.raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

    def raw_refresh(self, refresh_token=None):
        """
        Login in the platform with User credentials

        @return response
        """

        if not isinstance(self.token, Token):
            raise ValueError("Token must be a valid Token instance, use login before")

        if isinstance(refresh_token, str):
            if refresh_token.lower().startswith("bearer "):
                self.token.refresh_token = refresh_token.split("bearer ")[-1]
            else:
                self.token.refresh_token = refresh_token

        log.info("Refreshing token with controlpanel-auth host:{}, path:{}, refresh_token:{}"
                     .format(self.host, self.__control_panel_path, self.token.refresh_token))

        url = self.__refresh_template.substitute(
            protocol=self.protocol, host=self.hostport, path=self.__control_panel_path)
        body = self.token.refresh_token
        headers = {
            RestHeaders.ACCEPT_STR.value: RestHeaders.ACCEPT_ALL.value,
            RestHeaders.CONT_TYPE.value: RestHeaders.APP_JSON.value
            }
        response = self.call(RestMethods.POST.value, url, headers, data=body)

        if response.status_code == 200 and len(response.content):
            self.token = Token.from_json(response.json())
            log.info("Refresh correctly, access token: {}".format(self.token))
            self.add_to_debug_trace("Refresh correctly, access token: {}".format(self.token))

        else:
            log.info("Not possible to Refresh: {} - {}".format(response.status_code, response.text))
            self.add_to_debug_trace("Not possible to Refresh: {} - {}".format(response.status_code, response.text))

        return response

    def refresh(self, refresh_token=None):
        """
        Login in the platform with Iot-Client credentials

        @return ok, info
        """
        _ok = False
        _res = None

        try:
            response = self.raw_refresh(refresh_token=None)

            if response.status_code == 200 and len(response.content):
                _res = response.json()
                _ok = True

            else:
                _res = response.text

        except Exception as e:
            log.error("Not possible to refresh with controlpanel-auth host:{}, path:{}, refresh_token:{}"
                     .format(self.host, self.__control_panel_path, self.token.refresh_token))
            self.raise_exception_if_enabled(e)
            _res = e

        return _ok, _res
