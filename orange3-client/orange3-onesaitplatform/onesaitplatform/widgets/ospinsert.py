import time
import logging
from Orange.widgets import gui
from Orange.widgets.settings import Setting
from Orange.widgets.widget import OWWidget, Input, Output, Msg
from onesaitplatform.iotbroker.iotbrokerclient import \
    IotBrokerClient
from Orange.widgets.credentials import CredentialManager

log = logging.getLogger(__name__)

CONSOLE_DEBUG = False
def printt(msg):
    if CONSOLE_DEBUG:
        print("[{}] {}".format(time.ctime(), msg))

class OspInsert(OWWidget):

    name = "Insert"
    description = "Makes an ontology insert with iot-broker"
    icon = "icons/iot_client_insert.svg"

    # fields
    ontology = Setting("Restaurants")

    class Inputs:
        connection = Input("Client", object)
        instances = Input("Instances", list)

    want_main_area = False
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
        
        self.connection = None
        self.instances = []

        self._init_gui()
        self.validate_arguments()


    def _init_gui(self):
        printt("Init {} gui".format(__class__))
        log.info("Init {} gui".format(__class__))
        # conection parameters box
        self.box_parameters = gui.widgetBox(self.controlArea, "Query parameters")

        # ontology     
        gui.lineEdit(self.box_parameters, self, "ontology", "Enter ontology",
                    callback=self.ontology_changed,
                    valueType=str)

        gui.auto_commit(self.controlArea, self, "auto_commit", "Insert")

    @Inputs.connection
    def set_connection(self, connection):
        """Set input 'IotBrokerClient'"""    
        printt("Setting recieved connection: {}".format(connection))  
        log.info("Setting recieved connection: {}".format(connection))  
        if connection is not None:    
            self.connection = IotBrokerClient.from_json(connection)
        else:
            self.connection = connection

        self.validate_arguments()

    @Inputs.instances
    def set_instances(self, instances):
        """Set input instances"""
        printt("Setting recieved instances: {}".format(instances))          
        log.info("Setting recieved instances: {}".format(instances))          
        self.instances = instances
        self.validate_arguments()

    def ontology_changed(self):
        printt("Ontology changed")
        log.info("Ontology changed")
        self.validate_arguments()
        self.commit()
    
    def validate_arguments(self):
        printt("Validating arguments: {}".format(type(self.instances)))
        log.info("Validating arguments: {}".format(type(self.instances)))
        self._valid_arguments = True
        incorrects = []
        
        if self.ontology in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("ontology")
        
        if self.instances in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("instances")

        if self.connection is None:
            self._valid_arguments = False
            incorrects.append("connection")

        if len(incorrects) > 0:
            self.Error.custom("Incorrect arguments: {}".format(", ".join(incorrects)))
            log.error("Incorrect arguments: {}".format(", ".join(incorrects)))
        else:
            self.Error.custom.clear()
    
    def all_arguments_ok(self):
        self.validate_arguments()
        if self._valid_arguments:
            printt("Valid arguments: {}".format(self.instances))
            log.info("Valid arguments: {}".format(self.instances))
        else:
            printt("Not valid arguments: {}".format(self.instances))
            log.warn("Not valid arguments: {}".format(self.instances))
        
        return self._valid_arguments
    
    def handleNewSignals(self):
        """Reimplemeted from OWWidget."""
        printt("Handleing new signals...")
        log.info("Handleing new signals...")
        if self.connection is not None:
            self.commit()

    def start_or_restart_connection(self):
        if self.connection.is_connected:
            printt("Restarting connection: {}".format(self.connection))
            log.info("Restarting connection: {}".format(self.connection))
            self.connection.restart()
        else:
            printt("Starting new connection: {}".format(self.connection))
            log.info("Starting new connection: {}".format(self.connection))
            self.connection.join()
    
    def commit(self):
        self.start_and_insert_and_leave()
    
    def start_and_insert_and_leave(self):
        self.Warning.custom.clear()
        if self.all_arguments_ok():
            self.start_or_restart_connection()

            if self.connection.is_connected:
                self.make_insert()

            else:
                self.Warning.custom("Not possible to connect")

            self.close_connection()
        
    def make_insert(self):
        self.Warning.custom.clear()
        printt("Making insert: {} - {}".format(self.ontology, self.instances))
        log.info("Making insert: {} - {}".format(self.ontology, self.instances))
        is_done_insert, results_insert = self.connection.insert(self.ontology, self.instances)
        if is_done_insert:
            printt("Insert done: {} - {} results".format(is_done_insert, len(results_insert)))
            log.info("Insert done: {} - {} results".format(is_done_insert, len(results_insert)))
            self.results = results_insert
        else:
            self.results = None
            printt("Insert error: {} - {}".format(is_done_insert, results_insert))
            log.warn("Insert error: {} - {}".format(is_done_insert, results_insert))
            self.Warning.custom("Found some errors: Not possible to insert :(")
        

    def close_connection(self):
        printt("Closing conenction...")
        log.info("Closing conenction...")
        if self.connection.is_connected:
            self.connection.leave()

    def onDeleteWidget(self):
        printt("Deleting widget")
        log.info("Deleting widget")
        super().onDeleteWidget()
        if self.connection is not None:
            self.close_connection()

