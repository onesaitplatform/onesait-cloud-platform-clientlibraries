import os
import requests
from string import Template
import json
import logging
try:
    import onesaitplatform.common.config as config
    from onesaitplatform.enum import RestHeaders
    from onesaitplatform.enum import RestMethods
    from onesaitplatform.enum import RestProtocols
    from onesaitplatform.common.log import log
except Exception as e:
    print("Error - Not possible to import necesary libraries: {}".format(e))
try:
    log = logging.getLogger(__name__)
except:
    log.init_logging()


class FileManager:
    """
    Class FileManager to make operations with binary repository
    """
    binary_files_path = "/controlpanel/binary-repository"
    files_path = "/controlpanel/files"
    upload_template = Template("$protocol://$host$path")
    download_template = Template("$protocol://$host$path/$id_file")

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

    @property
    def avoid_ssl_certificate(self):
        return self.__avoid_ssl_certificate

    @avoid_ssl_certificate.setter
    def avoid_ssl_certificate(self, avoid_ssl_certificate):
        if self.protocol == RestProtocols.HTTPS.value:
            self.__avoid_ssl_certificate = avoid_ssl_certificate
        else:    
            self.__avoid_ssl_certificate = False

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

            url = self.upload_template.substitute(protocol=self.protocol,
                                                  host=self.host,
                                                  path=self.binary_files_path)
            headers = {RestHeaders.AUTHORIZATION.value: self.user_token}
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
            url = self.download_template.substitute(protocol=self.protocol,
                                                    host=self.host,
                                                    path=self.files_path,
                                                    id_file=id_file)
            headers = {RestHeaders.AUTHORIZATION.value: self.user_token,
                       RestHeaders.ACCEPT_STR.value: RestHeaders.ACCEPT_ALL.value}

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
