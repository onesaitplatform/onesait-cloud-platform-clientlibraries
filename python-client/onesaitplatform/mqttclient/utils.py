from threading import Event


class Message:
    def __init__(self, timeout=10):
        self._ready = Event()
        self._timeout = timeout
        self._response = None

    @property
    def result(self):
        received = self._ready.wait(timeout=self._timeout)
        if not received:
            raise MqttError("CONNECTION", "No Response Received")
        if not self._response['ok']:
            raise MqttError(self._response['errorCode'], self._response['error'])
        return self._response['data']

    @result.setter
    def result(self, dato):
        self._response = dato
        self._ready.set()

    def __len__(self):
        return len(self.result)

    def __getitem__(self, key):
        return self.result[key]

    def __iter__(self):
        return self.result.__iter__()

    def __contains__(self, key):
        return key in self.result


class MqttError(Exception):
    def __init__(self, error_code, description):
        self.error_code = error_code
        self.description = description
