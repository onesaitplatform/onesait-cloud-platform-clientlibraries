import os
import sys
relative_path = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
if relative_path not in sys.path:
    sys.path.insert(0, relative_path)

from digitalclient import DigitalClient

class DigitalClientTest():

    def __init__(self):
        HOST = "development.onesaitplatform.com"
        IOT_CLIENT = "RestaurantClient"
        IOT_CLIENT_TOKEN = "669b4309e9d24412b10b8cd34aa70d88"

        client = DigitalClient (HOST, iot_client=IOT_CLIENT, iot_client_token=IOT_CLIENT_TOKEN)
        client.protocol = "https"
        client.debug_mode = True
        client.proxies = {
            "http": "http://proxy.indra.es:8080",
            "https": "http://proxy.indra.es:8080"
            }
        self.client = client

    def test_join(self):
        _join, _res_join = self.client.join()
        query = "select c, _id from Restaurant as c limit 100"
        _ok, volcado_de_prueba = self.client.query('Restaurant', query, "SQL")
        print(_ok, volcado_de_prueba)

        self.client.session_key = "asdasd"
        query = "select c, _id from Restaurant as c where c.contextData.timestampMillis = 123 limit 100"
        _ok, volcado_de_prueba = self.client.query('Restaurant', query, "SQL")
        print(_ok, volcado_de_prueba)
        _restart, _res_restart = self.client.restart()
        


test = DigitalClientTest()
test.test_join()
