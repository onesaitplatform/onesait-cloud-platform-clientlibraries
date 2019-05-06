import time
import logging
from Orange.widgets import gui
from Orange.widgets.settings import Setting
from Orange.widgets.widget import OWWidget, Input, Output, Msg
from onesaitplatform.iotbroker import IotBrokerClient
from Orange.widgets.credentials import CredentialManager

log = logging.getLogger(__name__)

CONSOLE_DEBUG = False
def printt(msg):
    if CONSOLE_DEBUG:
        print("[{}] {}".format(time.ctime(), msg))

class OspQuery(OWWidget):

    name = "Query"
    description = "Makes an ontology query with iot-broker"
    icon = "icons/iot_client_query.svg"

    # fields
    ontology = Setting(None)
    query = Setting(None)
    query_type = None # default

    class Inputs:
        connection = Input("Client", object)

    class Outputs:
        results = Output("Results", list)

    want_main_area = False

    results = Setting(None)
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
        self.results = None
        self.auto_commit = False

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

        # query     
        gui.lineEdit(self.box_parameters, self, "query", "Enter query",
                    callback=self.query_changed,
                    valueType=str)
        
        gui.auto_commit(self.controlArea, self, "auto_commit", "Query")

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
            
    def ontology_changed(self):
        self.validate_arguments()
        printt("Setting new ontology: {}".format(self.query))
        log.info("Setting new ontology: {}".format(self.query))
        self.commit()
    
    def query_changed(self):
        printt("Setting new query: {}".format(self.query))
        log.info("Setting new query: {}".format(self.query))
        self.validate_arguments()
        self.commit()

    def set_query_type_from_query(self, query):
        if query is not None:
            printt("Setting new query type from {}".format(self.query))
            log.info("Setting new query type from {}".format(self.query))
            q_type = None
            if query.replace(" ", "").lower().startswith("db."):
                q_type = "NATIVE"
            elif query.replace(" ", "").lower().startswith("select"):
                q_type = "SQL"
            self.query_type = q_type

    def validate_arguments(self):
        self.set_query_type_from_query(self.query)
        printt("Validating arguments: {} - {} - {}".format(self.ontology, self.query, self.query_type))
        log.info("Validating arguments: {} - {} - {}".format(self.ontology, self.query, self.query_type))
        self._valid_arguments = True
        incorrects = []
        
        if self.ontology in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("ontology")

        if self.query in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("query")
        
        if self.query_type in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("query_type")

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
            printt("Valid arguments: {} - {} - {}".format(self.ontology, self.query, self.query_type))
            log.info("Valid arguments: {} - {} - {}".format(self.ontology, self.query, self.query_type))
        else:
            printt("Not valid arguments: {} - {} - {}".format(self.ontology, self.query, self.query_type))
            log.warn("Not valid arguments: {} - {} - {}".format(self.ontology, self.query, self.query_type))
        
        return self._valid_arguments
    
    def handleNewSignals(self):
        """Reimplemeted from OWWidget."""
        printt("Handleing new signals...")
        log.info("Handleing new signals...")
        if self.connection is not None:
            self.commit()
        
        else:
            self.results = None
            self.send_results()

    def start_or_restart_connection(self):
        if self.connection.is_connected:
            printt("Restarting connection: {}".format(self.connection))
            log.info("Restarting connection: {}".format(self.connection))
            self.connection.restart()
        else:
            printt("Starting new connection: {}".format(self.connection))
            log.warn("Starting new connection: {}".format(self.connection))
            self.connection.join()
    
    def commit(self):
        self.start_and_query_and_leave() 
    
    def start_and_query_and_leave(self):
        self.Warning.custom.clear()
        if self.all_arguments_ok():
            self.start_or_restart_connection()
            

            if self.connection.is_connected:
                self.make_query()

            else:
                self.Warning.custom("Not possible to connect")

            self.close_connection()
        
        self.send_results()

    def make_query(self):
        self.Warning.custom.clear()
        printt("Making query: {} - {} - {}".format(self.ontology, self.query, self.query_type))
        log.info("Making query: {} - {} - {}".format(self.ontology, self.query, self.query_type))
        is_done_query, results_query = self.connection.query(self.ontology, self.query, self.query_type)
        if is_done_query:
            printt("Query done: {} - {} results".format(is_done_query, len(results_query)))
            log.info("Query done: {} - {} results".format(is_done_query, len(results_query)))
            self.results = results_query
        else:
            self.results = None
            printt("Query error: {} - {}".format(is_done_query, results_query))
            log.warn("Query error: {} - {}".format(is_done_query, results_query))
            self.Warning.custom("Found some errors: Not possible to query :(")
        
    def send_results(self):
        printt("Sending results...")
        log.info("Sending results...")
        self.Outputs.results.send(self.results)

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
        self.results = None
        self.send_results()
