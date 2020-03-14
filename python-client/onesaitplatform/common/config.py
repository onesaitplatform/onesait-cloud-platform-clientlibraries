import os
import sys

# -------- Configuration --------
# Application
LOG_FOLDER = None

# Client
USER_AGENT = "onesaitplatform:PythonClient"
HOST = "www.onesaitplatform.online"
DEBUG_TRACE_LIMIT = 25
PROTOCOL = "https"

# Onesait Platform General
CONTROL_PANEL_HOST = "controlpanelservice"
CONTROL_PANEL_PATH = "/controlpanel"

# DigitalClient
IOT_BROKER_HOST = "iotbrokerservice"
IOT_BROKER_AVOID_SSL_CERTIFICATE = True
IOT_BROKER_PORT = 19000
IOT_BROKER_PATH = "/iot-broker"
IOT_CLIENT = "Client4Notebook"
IOT_CLIENT_TOKEN = "674a6e05348a468787e5af4acdf5b3df"
BATCH_QUERY_SIZE = 2000

# ApiManagerClient
API_MANAGER_HOST = "apimanagerservice"
API_MANAGER_PORT = 19100
API_MANAGER_AVOID_SSL_CERTIFICATE = True
API_MANAGER_PATH = "/api-manager/services/management"
API_CALLER_PATH = "/api-manager/server/api"
API_USER_TOKEN = "b32522cd73e84ddda519f1dff9627f40"

# FileManager
FILE_MANAGER_HOST = "controlpanelservice"
FILE_MANAGER_BINARY_FILES_PATH = "/controlpanel/binary-repository"
FILE_MANAGER_FILES_PATH = "/controlpanel/files"
USER_TOKEN = "Bearer ..."

# ModelServiceClient

