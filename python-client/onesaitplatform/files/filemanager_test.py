import os
import sys
import unittest
relative_path = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
if relative_path not in sys.path:
    sys.path.insert(0, relative_path)

from .filemanager import FileManager

class FileManagerTest(unittest.TestCase):

    HOST = "www.onesaitplatform.online"
    USER_TOKEN = "<token>"
    PROTOCOL = "https"
    AVOID_SSL_CERTIFICATE = True
    PROXIES = None

    def setUp(self):
        client = FileManager(self.HOST, user_token=self.USER_TOKEN)
        client.protocol = self.PROTOCOL
        client.proxies = self.PROXIES
        self.client = client
        self.client_json = {
            'host': self.HOST,
            'protocol': self.PROTOCOL,
            'user_token': self.USER_TOKEN,
            'proxies': self.PROXIES,
            'timeout': 10000,
            'avoid_ssl_certificate': self.AVOID_SSL_CERTIFICATE,
            'raise_exceptions': False
        }

    def test_init(self):
        self.assertEqual(self.client.host, self.HOST)
        self.assertEqual(self.client.user_token, self.USER_TOKEN)
        self.assertEqual(self.client.protocol, self.PROTOCOL)
        self.assertEqual(self.client.proxies, self.PROXIES)

    def test_to_json(self):
        self.assertEqual(self.client.to_json().keys(), self.client_json.keys())

    def test_from_json(self):
        tmp_client = FileManager.from_json(self.client.to_json())
        self.assertDictEqual(tmp_client.to_json(), self.client.to_json())

    @unittest.skip("Developing test for update file (skipping)")
    def test_update_file(self):
        HOST = "<host>"
        PROTOCOL = "https"
        TOKEN = "<token>"
        AVOID = True
        PROXIES = {
            "http": "<http_proxy>",
            "https": "<http_proxy>"
        }
        ID_FILE = "<id_file>"
        NAME_FILE = "<name_file>"
        PATH_FILE = r"<path_file>"
        METADATA = "File updated by python with love!"
        
        manager = FileManager(HOST, TOKEN)
        manager.protocol = PROTOCOL
        manager.avoid_ssl_certificate = AVOID
        manager.proxies = PROXIES
        _ok, _res = manager.update_file(ID_FILE, NAME_FILE, PATH_FILE, METADATA)
        self.assertTrue(_ok)



if __name__ == '__main__':
    unittest.main()
