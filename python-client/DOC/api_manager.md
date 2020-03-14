# Api Manager Example
## Create ApiManager
```python
import json
from onesaitplatform.apimanager import ApiManagerClient
```

```python
HOST = "development.onesaitplatform.com"
PORT = 443
TOKEN = "b32522cd73e84ddda519f1dff9627f40"
```

```python
client = ApiManagerClient(host=HOST)
```

## Set token
```python
client.setToken(TOKEN)
```

## Find APIs
```python
ok_find, res_find = client.find("RestaurantsAPI", "Created", "analytics")
print("API finded: {}".format(ok_find))
print("Api info:")
print(res_find)
```

## List APIs
```python
ok_list, res_list = client.list("analytics")
print("APIs listed {}".format(ok_list))
print("Apis info:")
for api in res_list:
    print(api)
    print("*")
```

## Make API request
```python
ok_request, res_request = client.request(method="GET", name="RestaurantsAPI/", version=1, body=None)
print("API request: {}".format(ok_request))
print("Api request:")
print(res_request)
```
