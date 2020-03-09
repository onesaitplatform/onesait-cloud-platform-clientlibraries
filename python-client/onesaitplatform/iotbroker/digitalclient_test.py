import os
import sys
import unittest
relative_path = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
if relative_path not in sys.path:
    sys.path.insert(0, relative_path)

from .digitalclient import DigitalClient

class DigitalClientTest(unittest.TestCase):

    HOST = "www.onesaitplatform.online"
    IOT_CLIENT = "<client>"
    IOT_CLIENT_TOKEN = "<token>"
    PROTOCOL = "https"
    AVOID_SSL_CERTIFICATE = True
    DEBUG_MODE = False
    PROXIES = None

    def setUp(self):
        client = DigitalClient(self.HOST, iot_client=self.IOT_CLIENT, iot_client_token=self.IOT_CLIENT_TOKEN)
        client.protocol = self.PROTOCOL
        client.debug_mode = self.DEBUG_MODE
        client.proxies = self.PROXIES
        self.client = client
        self.client_json = {
            'host': self.HOST,
            'port': None,
            'protocol': self.PROTOCOL,
            'iot_client': self.IOT_CLIENT,
            'iot_client_token': self.IOT_CLIENT_TOKEN,
            'is_connected': False,
            'session_key': None,
            'proxies': self.PROXIES,
            'timeout': 10000,
            'avoid_ssl_certificate': self.AVOID_SSL_CERTIFICATE,
            'raise_exceptions': False
        }

    def test_init(self):
        self.assertEqual(self.client.host, self.HOST)
        self.assertEqual(self.client.iot_client, self.IOT_CLIENT)
        self.assertEqual(self.client.iot_client_token, self.IOT_CLIENT_TOKEN)
        self.assertEqual(self.client.debug_mode, self.DEBUG_MODE)
        self.assertEqual(self.client.protocol, self.PROTOCOL)
        self.assertEqual(self.client.proxies, self.PROXIES)

    def test_to_json(self):
        self.assertEqual(self.client.to_json().keys(), self.client_json.keys())

    def test_from_json(self):
        tmp_client = DigitalClient.from_json(self.client.to_json())
        self.assertDictEqual(tmp_client.to_json(), self.client.to_json())


if __name__ == '__main__':
    unittest.main()
