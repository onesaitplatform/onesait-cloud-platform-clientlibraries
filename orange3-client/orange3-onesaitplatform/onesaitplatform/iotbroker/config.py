import os
import sys

## Configuration 
__installation_folder = os.path.dirname(__file__)
__configuration_file = os.path.join(__installation_folder, "config.json")

# Log
APP_NAME = "onesaitplatform-lib"
LOGS_FOLDER = os.path.join(os.path.expanduser("~"), ".onesaitplatform-lib")

## Variables
# Client
USER_AGENT = "onesaitplatform:PythonClient"
HOST = "development.onesaitplatform.com"
DEBUG_TRACE_LIMIT = 25

# Client.IotBrokerClient
IOT_CLIENT = "Client4Notebook"
IOT_CLIENT_TOKEN = "674a6e05348a468787e5af4acdf5b3df"

# Client.FileManager
USER_TOKEN = "Bearer ..."
