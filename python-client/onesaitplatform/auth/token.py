import time
import json
import logging

try:
    logging.basicConfig()
    log = logging.getLogger(__name__)
except:
    log.init_logging()


class Token:

    def __init__(self):
        self.access_token = None
        self.refresh_token = None
        self.token_type = None
        self.generation_time = time.time()
        self.expires_in = None
        self.scope = None
        self.principal = None
        self.clientId = None
        self.name = None
        self.grantType = None
        self.parameters = None
        self.authorities = []
        self.jti = None
        
    @property
    def expiration_time(self):
        exp_time = None
        if (self.generation_time is not None and self.expires_in is not None):
            log.warn("Invalid inputs for expiration time: generation-time={gen_time}, expires-in={exp_time}".format(gen_time=self.generation_time, exp_time=self.expires_in))
            if self.generation_time + self.expires_in > time.time():
                exp_time = self.generation_time + self.expires_in
        return exp_time

    @property
    def remaining_time(self):
        remaining_time = None
    
        if self.expiration_time is not None:
            remaining_time = max(0, time.time() - self.expiration_time)

        return remaining_time

    @property
    def access_token_str(self):
        access_token_str = None
        if self.access_token is not None and self.token_type is not None:
            access_token_str = "{token_type} {acc_token}".format(token_type=self.token_type, acc_token=self.access_token) 
        
        return access_token_str

    @property
    def refresh_token_str(self):
        refresh_token_str = None
        if self.refresh_token is not None and self.token_type is not None:
            refresh_token_str = "{token_type} {ref_token}".format(token_type=self.token_type, ref_token=self.refresh_token) 
        
        return refresh_token_str

    def __str__(self):
        return str(self.to_json())

    def to_json(self, as_string=False):
        """
        Export object to json

        @param as_string    If json dumped (String)

        @return json_obj    json-dict/ json string
        """
        json_obj = dict()
        json_obj["access_token"] = self.access_token
        json_obj["refresh_token"] = self.refresh_token
        json_obj["token_type"] = self.token_type
        json_obj["generation_time"] = self.generation_time
        json_obj["expires_in"] = self.expires_in
        json_obj["scope"] = self.scope        
        json_obj["principal"] = self.principal
        json_obj["clientId"] = self.clientId
        json_obj["name"] = self.name
        json_obj["grantType"] = self.grantType
        json_obj["parameters"] = self.parameters
        json_obj["authorities"] = self.authorities
        json_obj["jti"] = self.jti

        if as_string:
            json_obj = json.dumps(json_obj)

        log.info("Exported json {}".format(json_obj))
        return json_obj

    @staticmethod
    def from_json(json_object):
        """
        Creates a object from json-dict/ json-string

        @param json_object    json.dict/ json-string

        @return token         token object
        """
        client = None
        try:
            if type(json_object) == str:
                json_object = json.loads(json_object)
            
            json_object_keys = list(json_object.keys())
            token = Token()
            if "access_token" in json_object_keys:
                token.access_token = json_object['access_token']
            if "refresh_token" in json_object_keys:
                token.refresh_token = json_object['refresh_token']
            if "token_type" in json_object_keys:
                token.token_type = json_object['token_type'] 
            if "generation_time" in json_object_keys:
                token.generation_time = json_object['generation_time']
            if "expires_in" in json_object_keys:
                token.expires_in = json_object['expires_in']
            if "scope" in json_object_keys:
                token.scope = json_object['scope']
            if "principal" in json_object_keys:
                token.principal = json_object['principal']
            if "clientId" in json_object_keys:
                token.clientId = json_object['clientId']
            if "name" in json_object_keys:
                token.name = json_object['name']
            if "grantType" in json_object_keys:
                token.grantType = json_object['grantType']
            if "parameters" in json_object_keys:
                token.parameters = json_object['parameters']
            if "authorities" in json_object_keys:
                token.authorities = json_object['authorities']
            if "jti" in json_object_keys:
                token.jti = json_object['jti']

            log.info("Imported json {}".format(json_object))

        except Exception as e:
            log.error("Not possible to import object from json: {}".format(e))
        
        return token
