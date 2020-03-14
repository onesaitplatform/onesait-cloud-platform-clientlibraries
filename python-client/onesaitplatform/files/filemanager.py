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
        self.protocol = config.PROTOCOL
        self.avoid_ssl_certificate = False
    
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
    def avoid_ssl_certificate(self):
        return self.__avoid_ssl_certificate

    @avoid_ssl_certificate.setter
    def avoid_ssl_certificate(self, avoid_ssl_certificate):
        if self.protocol == RestProtocols.HTTPS.value:
            self.__avoid_ssl_certificate = avoid_ssl_certificate
        else:    
            self.__avoid_ssl_certificate = False

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

    def upload_file(self, filename, filepath):
        """
        Upload a file to binary-repository

        @param filename           file name
        @param filepath           file path
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

            response = requests.request(RestMethods.POST.value,
                                        url,
                                        headers=headers,
                                        files=files_to_up, 
                                        verify=not self.avoid_ssl_certificate)

            if response.status_code == 201:
                _ok = True
                _res = {"id": response.text,
                        "msg": "Succesfully uploaded file"
                        }

            else:
                raise Exception("Response: {} - {}"
                                .format(response.status_code, response.text))

        except Exception as e:
            log.error("Not possible to upload file: {}".format(e))
            _res = e

        return _ok, _res

    def download_file(self, id_file):
        """
        Download a file from binary-repository

        @param filename           file name
        @param filepath           file path
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

            response = requests.request(RestMethods.GET.value,
                                        url,
                                        headers=headers,
                                        data="",
                                        verify=not self.avoid_ssl_certificate)

            if response.status_code == 200:
                _ok = True
                name_file = get_name_from_response(response)
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
            _res = e
        
        return _ok, _res
