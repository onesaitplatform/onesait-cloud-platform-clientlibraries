# Ditial Client 
## Create DigitalClient
```python
import json
from onesaitplatform.iotbroker import DigitalClient
```

```python
HOST = "lab.onesaitplatform.com"
PORT = 443
IOT_CLIENT = "testtest"
IOT_CLIENT_TOKEN = "7320b00884ce4abfa06029017cb2472b"
```

```python
client = DigitalClient(HOST, port=PORT, iot_client=IOT_CLIENT, iot_client_token=IOT_CLIENT_TOKEN)
client.protocol = "https"
client.avoid_ssl_certificate = True
client.timeout = 10 * 1000
# If you are behind a proxy
client.proxies = {
    "http": "http://<business-proxy>:<port>",
    "https": "http://<business-proxy>:<port>"
}
```

```python
client.raise_exceptions = True
```

## Join/ start connection
```python
client.join()
# To see debug trace:
client.debug_trace
```

## Query data
```python
query = "select * from Restaurants limit 3"
ok_query, results_query = client.query(ontology="Restaurants", query=query, query_type="SQL")
print("Query success: {}".format(ok_query))
print("Query results:")
for res in results_query:
    print(res)
    print("*")
```

```python
# For raw request.Response:
raw_response = client.raw_query(ontology="Restaurants", query=query, query_type="SQL")
raw_response
```

```python
query = "db.Restaurants.find().limit(3)"
ok_query, results_query = client.query(ontology="Restaurants", query=query, query_type="NATIVE")
print("Query success: {}".format(ok_query))
print("Query results:")
for res in results_query:
    print(res)
    print("*")
```

## Query data in batch
```python
query_batch = "select c from Restaurants as c"
ok_query, results_query = client.query_batch(ontology="Restaurants", query=query_batch, query_type="SQL", batch_size=50)
print("Query success: {}".format(ok_query))
print("Query results:")
for res in results_query:
    print(res)
    print("*")
```

```python
query_batch = "db.Restaurants.find()"
ok_query, results_query = client.query_batch(ontology="Restaurants", query=query_batch, query_type="NATIVE", batch_size=50)
print("Query success: {}".format(ok_query))
print("Query results:")
for res in results_query:
    print(res)
    print("*")
```

## Insert data
```python
new_restaurant = {
    'Restaurant': {
        'address': {
            'building': '2780', 
            'coord': [-73.98241999999999, 40.579505], 
            'street': 'Stillwell Avenue', 
            'zipcode': '11224'
        }, 
        'borough': 'Brooklyn', 
        'cuisine': 'Edu', 
        'grades': [
            {'date': '2014-06-10T00:00:00Z', 'grade': 'A', 'score': 5}
        ], 
        'name': 'Riviera Caterer 18', 
        'restaurant_id': '40356118'
    }
}

new_restaurant_str = json.dumps(new_restaurant)
new_restaurants = [new_restaurant]
```

```python
ok_insert, res_insert = client.insert("Restaurants", new_restaurants)
print(ok_insert, res_insert)
```

```python
# For raw request.Response:
raw_response = client.raw_insert("Restaurants", new_restaurants)
raw_response
```

## Update data
```python
ontology = "testtest"
query_update = "db.testtest.update({'testtest.test':'test11'}, { $set:{'testtest.test':'test1'}})"
response_update = client.raw_update(ontology, query=query_update)
print(response_update.status_code)
if response_update.status_code == 200:
    print(response_update.json())
else:
    print(response_update.text)
```

```python
ontology = "testtest"
data_update = {'testtest.test':'test1'}
where_update = {'testtest.test':'test11'}
ok_update, res_update = client.update(ontology, data=data_update, where=where_update, return_ids=True)
print(res_update)
print(ok_update)
```

## Remove Data
```python
ontology = "testclientontology"
entity_id = "5e0f07097a5d0b000b723ffb"
response_raw_delete = client.raw_delete(ontology, entity_id, True)
print(response_raw_delete.status_code)
print(response_raw_delete.text)
ok_delete, res_delete = client.delete(ontology, entity_id, True)
print(ok_delete)
print(res_delete)
```

## Leave/ end connection
```python
client.leave()
```
