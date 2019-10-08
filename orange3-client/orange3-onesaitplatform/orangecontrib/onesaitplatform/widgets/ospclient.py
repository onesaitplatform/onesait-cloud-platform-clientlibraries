import time
import logging
from Orange.widgets import gui
from Orange.widgets.settings import Setting
from Orange.widgets.widget import Output, OWWidget, Msg
from onesaitplatform.iotbroker import DigitalClient
from Orange.widgets.credentials import CredentialManager

log = logging.getLogger(__name__)

CONSOLE_DEBUG = False
def printt(msg):
    if CONSOLE_DEBUG:
        print("[{}] {}".format(time.ctime(), msg))

class OspClient(OWWidget):
    # Widget's name as displayed in the canvas
    name = "Onesaitplatform Client"
    # Short widget description
    description = "Digital Client to login in Onesait Platform"

    # An icon resource file path for this widget
    # (a path relative to the module where this widget is defined)
    icon = "icons/client.svg"

    # Widget's outputs; 
    class Outputs:

        connection = Output("Client", str)

    # Basic (convenience) GUI definition:
    #   a simple 'single column' GUI layout
    want_main_area = False
    
    # fields
    host = Setting("www.onesaitplatform.online")
    iot_client = Setting("Client4Notebook")
    iot_client_token = Setting(None)
    avoid_ssl_certificate = Setting(False)
    connection = Setting(None)
    auto_commit = Setting(False)

    _valid_arguments = True

    class Information(OWWidget.Information):
        custom = Msg("{}")
    
    class Error(OWWidget.Error):
        custom = Msg("{}")

    class Warning(OWWidget.Warning):
        custom = Msg("{}")

    def __init__(self):
        printt("Init {} widget".format(__class__)) 
        log.info("Init {} widget".format(__class__)) 
        super().__init__() 

        self.iot_client_token = None # default
        self.avoid_ssl_certificate = False # default
        self.auto_commit = False

        self._init_gui()
        self._init_connection() 
        self.validate_arguments()
        
    def _init_gui(self):
        printt("Init {} gui".format(__class__))
        log.info("Init {} gui".format(__class__))
        # conection parameters box
        self.box_parameters = gui.widgetBox(self.controlArea, "Client parameters")

        # host     
        gui.lineEdit(self.box_parameters, self, "host", "Enter host",
                    callback=self.host_changed,
                    valueType=str)
        
        # iot_client
        gui.lineEdit(self.box_parameters, self, "iot_client", "Enter Digital Client",
                    callback=self.iot_client_changed,
                    valueType=str)
        
        # iot_client_token
        gui.lineEdit(self.box_parameters, self, "iot_client_token", "Enter Digital Client Token",
                    callback=self.iot_client_token_changed,
                    valueType=str)

        gui.checkBox(self.box_parameters, self, "avoid_ssl_certificate", "Avoid SSL certificate",
                    callback=self.avoid_ssl_certificate_changed)

        gui.auto_commit(self.controlArea, self, "auto_commit", "Send Client")

    def _init_connection(self):
        printt("Init {} connection".format(__class__))
        log.info("Init {} connection".format(__class__))
        if self.all_arguments_ok(): 
            self.connection = DigitalClient(host=self.host, iot_client=self.iot_client, iot_client_token=self.iot_client_token)
        printt("Created connection {}".format(self.connection)) 
        log.info("Created connection {}".format(self.connection)) 

    def send_on_creation(self):
        self.validate_arguments()
        if self.connection is not None and self.all_arguments_ok():
            if self.host != self.connection.host:
                self.connection.host = self.host

            if self.iot_client != self.connection.iot_client:
                self.connection.iot_client = self.iot_client

            if self.iot_client_token != self.connection.iot_client_token:
                self.connection.iot_client_token = self.iot_client_token

        self.commit()
    
    def commit(self):
        self.send_connection() 
    
    def send_connection(self):
        if self.all_arguments_ok():
            printt("Sending connection {}".format(self.connection))
            log.info("Sending connection {}".format(self.connection))
            if self.connection is not None:
                self.Outputs.connection.send(self.connection.to_json(as_string=True))
            else:
                self.Outputs.connection.send(self.connection)

    def start_or_restart_connection(self):
        printt("Start or restart connection")
        if self.connection.is_connected:
            self.connection.restart()
        else:
            self.connection.join()

        print("Client started: {}".format(self.connection.is_connected))

    def host_changed(self):
        self.validate_arguments()
        if self.host != self.connection.host:
            printt("Setting new host: {}".format(self.host))
            log.info("Setting new host: {}".format(self.host))
            self.connection.host = self.host

        self.commit()
    
    def iot_client_changed(self):
        self.validate_arguments()
        if self.iot_client != self.connection.iot_client:
            printt("Setting new client: {}".format(self.iot_client))
            log.info("Setting new client: {}".format(self.iot_client))
            self.connection.iot_client = self.iot_client

        self.commit()

    def iot_client_token_changed(self):
        self.validate_arguments()
        if self.iot_client_token != self.connection.iot_client_token:
            printt("Setting new token: {}".format(self.iot_client_token))
            log.info("Setting new token: {}".format(self.iot_client_token))
            self.connection.iot_client_token = self.iot_client_token
        
        self.commit()

    def avoid_ssl_certificate_changed(self):
        self.validate_arguments()
        if self.avoid_ssl_certificate != self.connection.avoid_ssl_certificate:
            printt("Setting avoid ssl: {}".format(self.avoid_ssl_certificate))
            log.info("Setting avoid ssl: {}".format(self.avoid_ssl_certificate))
            self.connection.avoid_ssl_certificate = self.avoid_ssl_certificate
        
        self.commit()

    def validate_arguments(self):
        printt("Validating arguments: {} - {} - {}".format(self.host, self.iot_client, self.iot_client_token))
        log.info("Validating arguments: {} - {} - {}".format(self.host, self.iot_client, self.iot_client_token))
        self._valid_arguments = True
        incorrects = []
        
        if self.host in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("host")

        if self.iot_client in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("iot_client")
        
        if self.iot_client_token in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("iot_client_token")

        if len(incorrects) > 0:
            self.Error.custom("Incorrect arguments: {}".format(", ".join(incorrects)))
            log.error("Incorrect arguments: {}".format(", ".join(incorrects)))

        else:
            self.Error.custom.clear()

    def all_arguments_ok(self):
        if self._valid_arguments:
            printt("Valid arguments: {} - {} - {}".format(self.host, self.iot_client, self.iot_client_token))
            log.info("Valid arguments: {} - {} - {}".format(self.host, self.iot_client, self.iot_client_token))
        else:
            printt("Not valid arguments: {} - {} - {}".format(self.host, self.iot_client, self.iot_client_token))
            log.warn("Not valid arguments: {} - {} - {}".format(self.host, self.iot_client, self.iot_client_token))
        
        return self._valid_arguments

    def onDeleteWidget(self):
        printt("Deletting widget...")
        log.info("Deletting widget...")
        super().onDeleteWidget()
        self.connection = None
        self.send_connection()


        

