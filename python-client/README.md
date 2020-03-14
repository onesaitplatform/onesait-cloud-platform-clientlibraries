Onesait Platform Python Client Services
===============================

## API documentation

This document describes the source code for the Onesait Platform Python Client Services.

This code provides several client classes which enable applications to connect to a Onesait Platform instance and perform basic CRUD operations.

Before using the client services for the first time, we strongly recommend that you learn the main concepts of the Onesait Platform platform. 

It supports Python 3.4+

## Installation instructions

In order to use this API, you must have `pip` installed in your system. The pip website (https://pypi.python.org/pypi/pip) contains detailed installation instructions.

Once `pip` is installed, you will be able to install the Python clients into your local package repository by running the following commands:

1. To install from download repository:

~~~~~~
pip install .
~~~~~~

~~~~~~
python setup.py install
~~~~~~

2. To install from pypi:

~~~~~~
pip install onesaitplatform-client-services
~~~~~~


## Samples usage

### DigitalClient

An example of DigitalClient is available in [DigitalClient tutorial](./examples/DigitalClient.ipynb)

[See documentation](./DOC/digital_broker.md)

### ApiManagerClient

An example of ApiManagerClient is available in [ApiManagerClient tutorial](./examples/ApiManagerClient.ipynb)

[See documentation](./DOC/api_manager.md)

### FileManager

An example of FileManager is available in [FileManager tutorial](./examples/FileManager.ipynb)

[See documentation](./DOC/file_manager.md)

### AuthClient

An example of AuthClient is available in [AuthClient tutorial](./examples/AuthClient.ipynb)

[See documentation](./DOC/auth_client.md)

### MQTTClient

An example of FileManager is available in [MqttClient tutorial](./examples/MqttClient.ipynb) (deprecated)

