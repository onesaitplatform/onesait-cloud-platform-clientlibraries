from sofia2api.core import Client
import json

if __name__ == '__main__':

    # Connection data
    host="rancher.sofia4cities.com"
    client_platform = "PythonAPITest"
    client_id = client_platform + '_pycharm'
    token = "871aa1cd991a46619b67a4b287492037"

    # Test Data
    ontology = "ontologyTest"
    data = {ontology: {"hhh": "value"}}
    query = '{ontologyTest.hhh":"value"}'
    update_native = "{$set: {'ontologyTest.modificado':'asd'}}"
    update_data = {'ontologyTest.newField': "Test"}

    client = Client(host, platform=client_platform, instance=client_id, token=token)

    client.connect()

    # QUERY
    resp = client.query(ontology, query)
    print(resp.result)

    # INSERT
    resp = client.insert(ontology, data)
    print(resp.result)
    resp = client.query(ontology, query)
    print(resp.result)
    # DELETE
    resp = client.delete(ontology, query)
    print(resp.result)
    resp = client.query(ontology, query)
    print(resp.result)

    # DISCONNECT
    client.disconnect()

    # Using the client as a context manager
    with Client("rancher.sofia4cities.com", platform=client_platform, instance=client_id, token=token) as client:

        # INSERT
        resp = client.insert(ontology, data)
        print(resp.result)
        inst_id = resp['id']
        resp = client.query(ontology, query)
        print(resp.result)

        # UPDATE
        resp = client.update(ontology, query, update_native)
        print(resp.result)
        resp = client.query(ontology, query)
        print(resp.result)

        # UPDATE BY ID
        resp = client.update_id(ontology, inst_id, update_data)
        print(resp.result)
        resp = client.query(ontology, query)
        print(resp.result)

        # DELETE BY ID
        resp = client.delete_id(ontology, inst_id)
        print(resp.result)
        resp = client.query(ontology, query)
        print(resp.result)
