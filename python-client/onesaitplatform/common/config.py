import os
import sys

# -------- Configuration --------
# Log
APP_NAME = "onesaitplatform-lib"
LOGS_FOLDER = os.path.join(os.path.expanduser("~"), ".onesaitplatform-lib")

# -------- Variables --------
# Client
USER_AGENT = "onesaitplatform:PythonClient"
HOST = "www.onesaitplatform.online"
DEBUG_TRACE_LIMIT = 25
PROTOCOL = "https"

# Client.IotBrokerClient
IOT_CLIENT = "Client4Notebook"
IOT_CLIENT_TOKEN = "674a6e05348a468787e5af4acdf5b3df"
BATCH_QUERY_SIZE = 2000

# Client.FileManager
USER_TOKEN = "Bearer ..."
