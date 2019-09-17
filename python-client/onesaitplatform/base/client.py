import os
import time
import json
import logging
try:
    import onesaitplatform.common.config as config
    from onesaitplatform.enum import RestProtocols
    from onesaitplatform.common.log import log
except Exception as e:
    print("Error - Not possible to import necesary libraries: {}".format(e))

try:
    logging.basicConfig()
    log = logging.getLogger(__name__)
except:
    log.init_logging()


class Client:
    """
    Class Client as base class of connections
    """
    user_agent = config.USER_AGENT
    debug_trace_limit = config.DEBUG_TRACE_LIMIT
    debug_mode = False

    __avoid_ssl_certificate = False

    def __init__(self, host, port=None):
        """
        Class Client as base class of connections

        @param host               Onesaitplatform host
        """
        self.host = host
        self.port = port
        self.__timeout = None
        self.__proxies = None
        self.__raise_exceptions = False
        self.protocol = config.PROTOCOL
        self.avoid_ssl_certificate = False
        self.is_connected = False
        self.debug_trace = []
        self.load_proxies_automatically()

    @property
    def hostport(self):
        hostport = self.host
        if self.port is not None:
            hostport += ":{}".format(self.port)
        return hostport

    @property
    def timeout(self):
        return self.__timeout

    @timeout.setter
    def timeout(self, timeout):
        try:
            timeout = int(timeout)
        except:
            timeout = None
        self.__timeout = timeout

    @property
    def proxies(self):
        return self.__proxies

    @proxies.setter
    def proxies(self, proxies):
        if not isinstance(proxies, dict):
            proxies = None
        self.__proxies = proxies
    
    @property
    def protocol(self):
        return self.__protocol

    @protocol.setter
    def protocol(self, protocol):
        if protocol == RestProtocols.HTTPS.value:
            self.__protocol = protocol
        else:    
            self.__protocol = RestProtocols.HTTP.value

    @property
    def avoid_ssl_certificate(self):
        return self.__avoid_ssl_certificate

    @avoid_ssl_certificate.setter
    def avoid_ssl_certificate(self, avoid_ssl_certificate):
        if self.protocol == RestProtocols.HTTPS.value:
            self.__avoid_ssl_certificate = avoid_ssl_certificate
        else:    
            self.__avoid_ssl_certificate = False

    @property
    def raise_exceptions(self):
        return self.__raise_exceptions

    @raise_exceptions.setter
    def raise_exceptions(self, raise_exceptions):
        if raise_exceptions == True or raise_exceptions == 1:
            self.__raise_exceptions = True
        else:    
            self.__raise_exceptions = False
    
    def add_to_debug_trace(self, msg):
        """
        Add a message to debug trace list
        """
        to_add = "[{}] {}".format(time.ctime(), msg)
        if self.debug_mode:
            print(to_add)
        if len(self.debug_trace) == self.debug_trace_limit:
            self.debug_trace.pop()
        self.debug_trace.append(to_add)

    def to_json(self, as_string=False):
        """
        Export object to json

        @param as_string    If json dumped (String)

        @return json_obj   json-dict/ json string
        """
        self.debug_trace = []
        json_obj = dict(self.__dict__)
        json_obj.pop("debug_trace", None)
        if as_string:
            json_obj = json.dumps(json_obj)

        log.info("Exported json {}".format(json_obj))
        self.add_to_debug_trace("Exported json {}".format(json_obj))
        return json_obj

    def is_correct_status_code(self, status_code):
        assert isinstance(status_code, int), "Invalid input value"
        return (status_code >= 200) and (status_code < 300)

    def load_proxies_automatically(self):
        proxies = {}
        if "http_proxy" in os.environ.keys():
            proxies["http"] = os.environ["http_proxy"]
        elif "HTTP_PROXY" in os.environ.keys():
            proxies["http"] = os.environ["HTTP_PROXY"]

        if "https_proxy" in os.environ.keys():
            proxies["https"] = os.environ["https_proxy"]
        elif "HTTPS_PROXY" in os.environ.keys():
            proxies["https"] = os.environ["HTTPS_PROXY"]

        if proxies != {}:
            log.info("Detected proxies: {}".format(proxies))
            self.add_to_debug_trace("Detected proxies: {}".format(proxies))
            self.__proxies = proxies

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
            connection = Client(host=json_object['host'])
            connection.is_connected = json_object['is_connected']

            log.info("Imported json {}".format(json_object))
            connection.add_to_debug_trace("Imported json {}"
                                          .format(json_object))

        except Exception as e:
            log.error("Not possible to import object from json: {}".format(e))

        return connection

    def raise_exception_if_enabled(self, exception):
        if self.raise_exceptions:
            assert isinstance(exception, Exception)
            raise exception
