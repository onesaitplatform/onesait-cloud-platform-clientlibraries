import time
import json
try:
    from onesaitplatform.common.log import log
    import onesaitplatform.common.config as config
except Exception as e:
    print("Error - Not possible to import necesary libraries: {}".format(e))


class Client:
    """
    Class Client as base class of connections
    """
    user_agent = config.USER_AGENT
    debug_trace_limit = config.DEBUG_TRACE_LIMIT
    debug_mode = False

    def __init__(self, host=config.HOST):
        """
        Class Client as base class of connections

        @param host               Onesaitplatform host
        """
        self.host = host
        self.is_connected = False
        self.debug_trace = []

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
