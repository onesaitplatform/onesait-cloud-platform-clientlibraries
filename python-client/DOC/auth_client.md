# AuthClient example
## Create AuthClient
```python
from onesaitplatform.auth import AuthClient

credentials = {
    "username": "<username>",
    "password": "<password>",
    "vertical": "onesaitplatform"
    }

client = AuthClient(host="lab.onesaitplatform.com")
client.protocol = "https"
client.avoid_ssl_certificate = True
```

## Login
```python
ok_login, res_login = client.login(username=self.credentials["username"], password=self.credentials["password"])
print(ok_login)
if ok_login:
    print(res_login)
    print(client.token)
```

```python
# For raw request.Response:
res_login = client.raw_login(username=self.credentials["username"], password=self.credentials["password"])
print(res_login)
```

## Refresh token
```python
ok_refresh, res_refresh = client.refresh()
print(ok_refresh)
if ok_refresh:
    print(res_refresh)
    print(client.token)
```

```python
# For raw request.Response:
res_refresh = client.raw_refresh()
print(res_refresh)
```
