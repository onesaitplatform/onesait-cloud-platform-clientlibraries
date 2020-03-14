import os
import sys
import unittest
relative_path = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
if relative_path not in sys.path:
    sys.path.insert(0, relative_path)

from onesaitplatform.auth.token import Token


class TokenTest(unittest.TestCase):

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

    def test_from_json(self):
        token = Token.from_json(self.example_json_token)
        for key in self.example_json_token.keys():
            self.assertEqual(self.example_json_token.get(key), getattr(token, key))

    def test_to_json(self):
        token_json = self.example_token.to_json()
        for key in self.example_json_token:
            self.assertIn(key, token_json)
            self.assertEqual(getattr(self.example_token, key), token_json[key])


if __name__ == '__main__':
    unittest.main()
