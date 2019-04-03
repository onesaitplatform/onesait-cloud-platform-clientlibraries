import paho.mqtt.client as mqtt
import json
from threading import Event
from uuid import uuid4

from onesaitplatform.mqttclient.utils import Message


class Client:

    def __init__(self, host, port=1883, platform=None, instance=None, token=None, timeout=10):
        self._host = host
        self._port = port

        self._platform = platform
        self._instance = instance
        self._token = token

        self._timeout = timeout

        self._client = mqtt.Client(instance)
        self._client.on_connect = self._on_connect
        self._client.on_message = self._on_message

        self._conected_event = Event()
        self._messages = {}
        self._session_key = None

    @property
    def _channel(self):
        return "/topic/message/{}".format(self._instance)

    def connect(self):
        self._client.connect(self._host)
        self._client.loop_start()
        self._conected_event.wait()
        self.join()

    def disconnect(self):
        self.leave()
        self._session_key = None
        self._client.loop_stop()
        self._client.disconnect()

    def join(self):
        msg = {
            "@type": "SSAPBodyJoinMessage",
            "token": self._token,
            "clientPlatform": self._platform,
            "clientPlatformInstance": self._instance,
            "sessionKeyMandatory": False,
            "ontologyMandatory": False
        }
        ret = self._send_msg("JOIN", msg)
        self._session_key = ret.result['sessionKey']

    def leave(self):
        return
        msg = {
            "@type": "SSAPBodyLeaveMessage",
            "clientPlatform": self._platform,
            "clientPlatformInstance": self._instance,
            "sessionKeyMandatory": True,
            "ontologyMandatory": False
        }
        ret = self._send_msg("LEAVE", msg)
        self._session_key = ret.result['sessionKey']
        print("DESCONECTADO con sesion: {}".format(self._session_key))

    def query(self, ontology, query, query_type="NATIVE"):
        msg = {
            "@type": "SSAPBodyQueryMessage",
            "ontology": ontology,
            "query": 'db.{}.find({})'.format(ontology, query),
            "queryType": query_type,
            "resultFormat": None,
            "sessionKeyMandatory": True,
            "ontologyMandatory": True
        }
        resp = self._send_msg("QUERY", msg)
        return resp

    def insert(self, ontology, data):
        msg = {
            "@type": "SSAPBodyInsertMessage",
            "ontology": ontology,
            "data": data,
            "sessionKeyMandatory": True,
            "ontologyMandatory": True
        }
        resp = self._send_msg("INSERT", msg)
        return resp

    def delete(self, ontology, query):
        msg = {
            "@type": "SSAPBodyDeleteMessage",
            "ontology": ontology,
            "query": "db.{}.remove({})".format(ontology, query),
            "sessionKeyMandatory": True,
            "ontologyMandatory": True
        }
        resp = self._send_msg("DELETE", msg)
        return resp

    def delete_id(self, ontology, inst_id):
        msg = {
            "@type": "SSAPBodyDeleteByIdMessage",
            "ontology": ontology,
            "id": inst_id,
            "sessionKeyMandatory": True,
            "ontologyMandatory": True
        }
        resp = self._send_msg("DELETE_BY_ID", msg)
        return resp

    def update(self, ontology, query, set):
        msg = {
            "@type": "SSAPBodyUpdateMessage",
            "ontology": ontology,
            "query": "db.{}.update({}, {})".format(ontology, query, set),
            "sessionKeyMandatory": True,
            "ontologyMandatory": True
        }
        resp = self._send_msg("UPDATE", msg)
        return resp

    def update_id(self, ontology, inst_id, data):
        msg = {
            "@type": "SSAPBodyUpdateByIdMessage",
            "ontology": ontology,
            "id": inst_id,
            "data": data,
            "sessionKeyMandatory": True,
            "ontologyMandatory": True
        }
        resp = self._send_msg("UPDATE_BY_ID", msg)
        return resp

    # The callback for when the client receives a CONNACK response from the server.
    def _on_connect(self, client, userdata, flags, rc):
        # Subscribing in on_connect() means that if we lose the connection and
        # reconnect then subscriptions will be renewed.
        self._client.subscribe("$SYS/#")
        self._client.subscribe(self._channel)
        self._conected_event.set()

    # The callback for when a PUBLISH message is received from the server.
    def _on_message(self, client, userdata, msg):
        message = json.loads(msg.payload)
        if message['direction'] == 'REQUEST':
            return
        if message['messageId'] is None:
            print(message)
        resp = self._messages.pop(message['messageId'])
        resp.result = message['body']

    def _send_msg(self, type, body, timeout=None):
        msg_id = uuid4().hex
        msg = {
            "sessionKey": self._session_key,
            "messageId": msg_id,
            "direction": "REQUEST",
            "messageType": type,
            "body": body
        }
        self._messages[msg_id] = Message(timeout=self._timeout)

        str_msg = json.dumps(msg)
        self._client.publish(self._channel, str_msg)

        return self._messages[msg_id]

    def __enter__(self):
        self.connect()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.disconnect()
        return False
