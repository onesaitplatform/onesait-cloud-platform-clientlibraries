import time
import json
import logging
import pandas as pd
from Orange.widgets.widget import OWWidget, Input, Output, Msg
from Orange.data import Table
import orangecontrib.onesaitplatform.utils.dataconversion as udata

log = logging.getLogger(__name__)


CONSOLE_DEBUG = False
def printt(msg):
    if CONSOLE_DEBUG:
        print("[{}] {}".format(time.ctime(), msg))

class OspFlatData(OWWidget):
    name = "Flat data"
    description = "Flat data from quer to convert data to Orange table"
    icon = "icons/flat_data.svg"

    class Inputs:
        in_json_data = Input("Json data", list)

    class Outputs:
        out_data = Output("Table data", Table)

    want_main_area = False

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

        self.in_json_data = None
        self.out_data = None

    @Inputs.in_json_data
    def set_in_json_data(self, in_json_data):
        """Set input 'in_json_data'."""
        printt("Setting recieved json_data")  
        log.info("Setting recieved json_data")  
        self.in_json_data = in_json_data


    def handleNewSignals(self):
        """Reimplemeted from OWWidget."""
        printt("Handleing new signals...")
        log.info("Handleing new signals...")
        if self.in_json_data is not None:
            self.commit()
        else:
            # Clear the channel by sending `None`
            self.commit()

    def commit(self):
        self.convert_data()
    
    def convert_data(self):
        printt("Converting data")
        log.info("Converting data")
        if self.in_json_data is not None:
            self.out_data = udata.json_data_to_orange_table(self.in_json_data)
            printt("Converted data")
            log.info("Converted data")

        self.Outputs.out_data.send(self.out_data)


