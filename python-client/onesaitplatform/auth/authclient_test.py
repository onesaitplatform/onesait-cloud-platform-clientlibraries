import os
import sys
import unittest
relative_path = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
if relative_path not in sys.path:
    sys.path.insert(0, relative_path)

from onesaitplatform.auth.token import Token
from onesaitplatform.auth.authclient import AuthClient


class AuthClientTest(unittest.TestCase):

    def setUp(self):
        self.example_json_token = {
            "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmluY2lwYWwiOiJhZG1pbmlzdHJhdG9yIiwiY2xpZW50SWQiOiJvbmVzYWl0cGxhdGZvcm0iLCJ1c2VyX25hbWUiOiJhZG1pbmlzdHJhdG9yIiwic2NvcGUiOlsib3BlbmlkIl0sIm5hbWUiOiJhZG1pbmlzdHJhdG9yIiwiZXhwIjoxNTgzMjc2NzAwLCJncmFudFR5cGUiOiJwYXNzd29yZCIsInBhcmFtZXRlcnMiOnsidmVydGljYWwiOm51bGwsImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInVzZXJuYW1lIjoiYWRtaW5pc3RyYXRvciJ9LCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOSVNUUkFUT1IiXSwianRpIjoiMWI2ODYyZDktOThkMy00YzM1LWEwYjktNDFhYWViZTVjMjRmIiwiY2xpZW50X2lkIjoib25lc2FpdHBsYXRmb3JtIn0.Rs8uSMltAhaALbTaDg7_OwShAGk3OboHWnsgZZWm0xY",
            "token_type": "bearer",
            "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmluY2lwYWwiOiJhZG1pbmlzdHJhdG9yIiwiY2xpZW50SWQiOiJvbmVzYWl0cGxhdGZvcm0iLCJ1c2VyX25hbWUiOiJhZG1pbmlzdHJhdG9yIiwic2NvcGUiOlsib3BlbmlkIl0sImF0aSI6IjFiNjg2MmQ5LTk4ZDMtNGMzNS1hMGI5LTQxYWFlYmU1YzI0ZiIsIm5hbWUiOiJhZG1pbmlzdHJhdG9yIiwiZXhwIjoxNTg0NjA1Nzg2LCJncmFudFR5cGUiOiJwYXNzd29yZCIsInBhcmFtZXRlcnMiOnsidmVydGljYWwiOm51bGwsImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInVzZXJuYW1lIjoiYWRtaW5pc3RyYXRvciJ9LCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOSVNUUkFUT1IiXSwianRpIjoiM2ZlM2YwNjEtMzhhNS00MGE4LWIxZTMtYjk2Zjk2Y2U0Yzg2IiwiY2xpZW50X2lkIjoib25lc2FpdHBsYXRmb3JtIn0.Fjlrsl6Th7Ttd3WSBDEFO2Gb-FGHZjwF6R7i6l31LoE",
            "expires_in": 21098,
            "scope": "openid",
            "principal": "administrator",
            "clientId": "onesaitplatform",
            "name": "administrator",
            "grantType": "password",
            "parameters": {
                "grant_type": "password",
                "vertical": None,
                "username": "administrator"
            },
            "authorities": [
                "ROLE_ADMINISTRATOR"
            ],
            "jti": "1b6862d9-98d3-4c35-a0b9-41aaebe5c24f"
        }

        self.example_token = Token()
        self.example_token.access_token = self.example_json_token['access_token']
        self.example_token.refresh_token = self.example_json_token['refresh_token']
        self.example_token.token_type = self.example_json_token['token_type']
        self.example_token.expires_in = self.example_json_token['expires_in']
        self.example_token.scope = self.example_json_token['scope']
        self.example_token.principal = self.example_json_token['principal']
        self.example_token.clientId = self.example_json_token['clientId']
        self.example_token.name = self.example_json_token['name']
        self.example_token.grantType = self.example_json_token['grantType']
        self.example_token.parameters = self.example_json_token['parameters']
        self.example_token.authorities = self.example_json_token['authorities']
        self.example_token.jti = self.example_json_token['jti']

        self.example_json_auth_client = {
            "host": "host",
            "username": "username",
            "password": "password",
            "vertical": "onesaitplatform",
            "token": self.example_json_token
        }

        self.example_auth_client = AuthClient(
            host=self.example_json_auth_client["host"],
            username=self.example_json_auth_client["username"],
            password=self.example_json_auth_client["password"])
        self.example_auth_client.token = self.example_token

        # replace with real credentials
        self.credentials = {
            "username": "<username>",
            "password": "<password>",
            "vertical": "onesaitplatform"
        }

    def test_from_json(self):
        client = AuthClient.from_json(self.example_json_auth_client)
        for key in self.example_json_auth_client.keys():
            if key == "token":
                token = Token.from_json(self.example_json_token)
                for token_key in self.example_json_auth_client[key]:
                    self.assertEqual(self.example_json_token.get(token_key), getattr(token, token_key))
            else:
                self.assertEqual(self.example_json_auth_client.get(key), getattr(client, key))

    def test_to_json(self):
        auth_json = self.example_auth_client.to_json()
        for key in self.example_json_auth_client:
            if key == "token":
                self.assertEqual(getattr(self.example_auth_client, key).to_json(), auth_json[key])
            else:
                self.assertIn(key, auth_json)
                self.assertEqual(getattr(self.example_auth_client, key), auth_json[key])

    @unittest.skip('Real credentials are necessary')
    def test_raw_login(self):
        client = AuthClient(host="lab.onesaitplatform.com")
        client.protocol = "https"
        client.avoid_ssl_certificate = True
        res_login = client.raw_login(username=self.credentials["username"], password=self.credentials["password"])
        self.assertEqual(res_login.status_code, 200)
        self.assertIsInstance(client.token, Token)

    @unittest.skip('Real credentials are necessary')
    def test_login(self):
        client = AuthClient(host="lab.onesaitplatform.com")
        client.protocol = "https"
        client.avoid_ssl_certificate = True
        ok_login, res_login = client.login(username=self.credentials["username"], password=self.credentials["password"])
        self.assertTrue(ok_login)
        self.assertIsInstance(client.token, Token)
        self.assertTrue(client.token_str.startswith("bearer "))

    @unittest.skip('Real credentials are necessary')
    def test_raw_refresh(self):
        client = AuthClient(host="lab.onesaitplatform.com")
        client.protocol = "https"
        client.avoid_ssl_certificate = True
        res_login = client.raw_login(username=self.credentials["username"], password=self.credentials["password"])
        if res_login.status_code == 200:
            res_refresh = client.raw_refresh()
            self.assertEqual(res_refresh.status_code, 200)
            self.assertIsInstance(client.token, Token)

    @unittest.skip('Real credentials are necessary')
    def test_refresh(self):
        client = AuthClient(host="lab.onesaitplatform.com")
        client.protocol = "https"
        client.avoid_ssl_certificate = True
        ok_login, res_login = client.login(username=self.credentials["username"], password=self.credentials["password"])
        if ok_login:
            ok_refresh, res_refresh = client.refresh()
            self.assertTrue(ok_refresh)
            self.assertIsInstance(client.token, Token)


if __name__ == '__main__':
    unittest.main()
