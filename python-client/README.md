Sofia4Cities Python Client API
============================

## API documentation

This document describes the source code for the Sofia2 Python MQTT API Client.

This code provides a client class which enable applications to connect to a Sofia2 instance and perform basic CRUD operations.

Before using the SSAP API for the first time, we strongly recommend that you learn the main concepts of the Sofia4Cities platform. 

It supports Python 3.4+

## Installation instructions

In order to use this API, you must have `pip` installed in your system. The pip website (https://pypi.python.org/pypi/pip) contains detailed installation instructions.

Once `pip` is installed, you will be able to install the Python API into your local package repository by running the following command:

	pip install -r requirements.txt
	
	python setup.py install


Sample usage:

	from sofia2api import Client
	ontology = "ontologyTest"
	data = {ontology: {"hhh": "value"}}
	query = '{ontologyTest.hhh":"value"}'
	update_native = "{$set: {'ontologyTest.modificado':'asd'}}"
	update_data = {'ontologyTest.newField': "Test"}
	
	with Client(host, platform=client_platform, instance=client_id, token=token) as client:
	    # INSERT
	    resp = client.insert(ontology, data)
	
	    # QUERY
	    resp = client.query(ontology, query)
	
	    # DELETE
	    resp = client.delete(ontology, query)
	
	    # UPDATE
	    resp = client.update(ontology, query, update_native)
	
	    # UPDATE BY ID
	    resp = client.update_id(ontology, inst_id, update_data)
	
	    # DELETE BY ID
	    resp = client.delete_id(ontology, inst_id)


A complete executable script can be find in:

    \sofia2api\test_all.py.py

