import os
import requests
from string import Template
import json
import logging
import onesaitplatform.common.config as config
from onesaitplatform.enums import RestHeaders
from onesaitplatform.enums import RestMethods
from onesaitplatform.enums import RestProtocols
from onesaitplatform.common.log import log

try:
    logging.basicConfig()
    log = logging.getLogger(__name__)
except:
    log.init_logging()


class FileManager:
    """
    Class FileManager to make operations with binary repository
    """
    __binary_files_path = config.FILE_MANAGER_BINARY_FILES_PATH
    __files_path = config.FILE_MANAGER_FILES_PATH
    __upload_template = Template("$protocol://$host$path")
    __update_template = Template("$protocol://$host$path/$id_file")
    __download_template = Template("$protocol://$host$path/$id_file")
    __MAX_X_OP_APIKEY_LENGTH = 35

    __avoid_ssl_certificate = False

    def __init__(self, host, user_token=config.USER_TOKEN):
        """
        Class FileManager to make operations with binary repository

        @param host               Onesaitplatform host
        @param user_token         Onesaitplatform user-token
        """
        self.host = host
        self.user_token = user_token
        self.__protocol = config.PROTOCOL
        self.avoid_ssl_certificate = False
        self.__proxies = None
        self.__timeout = None
        self.__raise_exceptions = False
        self.load_proxies_automatically()
    
    @property
    def protocol(self):
        return self.__protocol

    @protocol.setter
    def protocol(self, protocol):
        if protocol == RestProtocols.HTTPS.value:
            self.__protocol = protocol
        else:    
            self.__protocol = RestProtocols.HTTP.value
            self.__avoid_ssl_certificate = False

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

    @property
    def __headers(self):
        _headers =  dict()
        if len(self.user_token) < self.__MAX_X_OP_APIKEY_LENGTH:
            _headers[RestHeaders.X_OP_APIKey.value] = self.user_token
        else:
            _headers[RestHeaders.AUTHORIZATION.value] = self.user_token

        return _headers

    @property
    def __headers_download(self):
        _headers =  dict(self.__headers)
        _headers[RestHeaders.ACCEPT_STR.value] = RestHeaders.ACCEPT_ALL.value
        return _headers
    
    def __str__(self):
        """
        String to print object info
        """
        hide_attributes = []
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
        json_obj["protocol"] = self.protocol
        json_obj["user_token"] = self.user_token
        json_obj["proxies"] = self.proxies
        json_obj["timeout"] = self.timeout
        json_obj["avoid_ssl_certificate"] = self.avoid_ssl_certificate
        json_obj["raise_exceptions"] = self.raise_exceptions

        if as_string:
            json_obj = json.dumps(json_obj)

        log.info("Exported json {}".format(json_obj))
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
            client = FileManager(host=json_object['host'])
            if "user_token" in json_object_keys:
                client.user_token = json_object['user_token'] 
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

        except Exception as e:
            log.error("Not possible to import object from json: {}".format(e))

        return client

    def __raise_exception_if_enabled(self, exception):
        if self.raise_exceptions:
            assert isinstance(exception, Exception)
            raise exception

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
            self.__proxies = proxies

    def upload_file(self, filename, filepath, metadata = None):
        """
        Upload a file to binary-repository

        @param filename           file name
        @param filepath           file path
        @param metadata           file metadata or description
        """
        _ok = False
        _res = None
        try:
            if not os.path.exists(filepath):
                raise IOError("Source file not found: {}".format(filepath))

            url = self.__upload_template.substitute(protocol=self.protocol,
                                                  host=self.host,
                                                  path=self.__binary_files_path)
            headers = self.__headers
            files_to_up = {'file': (
                filename,
                open(filepath, 'rb'),
                "multipart/form-data"
                )}
            params = {
                "metadata": metadata
            }

            response = requests.request(RestMethods.POST.value, url,
                                        params=params,
                                        headers=headers, files=files_to_up, 
                                        verify=not self.avoid_ssl_certificate,
                                        timeout=self.timeout, proxies=self.proxies)

            if response.status_code == 201:
                _ok = True
                _res = {"id": response.text,
                        "msg": "Succesfully uploaded file"
                        }

            else:
                raise Exception("Response: {status_code} - {text}"
                                .format(status_code=response.status_code, text=response.text))

        except Exception as e:
            log.error("Not possible to upload file: {}".format(e))
            self.__raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

    def update_file(self, id_file, filename, filepath, metadata = None):
        """
        Upload a file to binary-repository

        @param id_file            file id in onesait platform
        @param filename           file name
        @param filepath           file path
        @param metadata           file metadata or description
        """
        _ok = False
        _res = None
        try:
            if not os.path.exists(filepath):
                raise IOError("Source file not found: {}".format(filepath))

            url = self.__update_template.substitute(protocol=self.protocol,
                                                    host=self.host,
                                                    path=self.__binary_files_path,
                                                    id_file=id_file)
            
            headers = self.__headers
            files_to_up = {'file': (
                filename,
                open(filepath, 'rb'),
                "multipart/form-data"
                )}
            params = {
                "metadata": metadata
            }

            response = requests.request(RestMethods.PUT.value, url,
                                        params=params,
                                        headers=headers, files=files_to_up, 
                                        verify=not self.avoid_ssl_certificate,
                                        timeout=self.timeout, proxies=self.proxies)

            if response.status_code == 202:
                _ok = True
                _res = {"id": response.text,
                        "msg": "Succesfully uploaded file"
                        }

            else:
                raise Exception("Response: {} - {}"
                                .format(response.status_code, response.text))

        except Exception as e:
            log.error("Not possible to upload file: {}".format(e))
            self.__raise_exception_if_enabled(e)
            _res = e

        return _ok, _res

    def download_file(self, id_file, filepath = ""):
        """
        Download a file from binary-repository

        @param id_file            file id in onesait platform
        @param filepath           file path to be saved
        """
        
        def get_name_from_response(response):
            name_getted = None
            key_name = 'Content-Disposition'
            name_getted = response.headers[key_name].replace(" ", "").split("=")[1].strip()
            return name_getted
        
        _ok = False
        _res = None
        try:
            url = self.__download_template.substitute(protocol=self.protocol,
                                                    host=self.host,
                                                    path=self.__files_path,
                                                    id_file=id_file)
            headers = self.__headers_download

            response = requests.request(RestMethods.GET.value, url,
                                        headers=headers, data="",
                                        verify=not self.avoid_ssl_certificate,
                                        timeout=self.timeout, proxies=self.proxies)

            if response.status_code == 200:
                _ok = True
                name_file = get_name_from_response(response)
                if os.path.isdir(filepath):
                    name_file = os.path.join(filepath, name_file)
                _res = {"id": id_file,
                        "msg": "Succesfully downloaded file",
                        "name": name_file
                        }

                with open(name_file, 'wb') as f:
                    f.write(response.content)

            else:
                raise Exception("Response: {} - {}"
                                .format(response.status_code, response.text))

        except Exception as e:
            log.error("Not possible to download file: {}".format(e))
            self.__raise_exception_if_enabled(e)
            _res = e
        
        return _ok, _res
