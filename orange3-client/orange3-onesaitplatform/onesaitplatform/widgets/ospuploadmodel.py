import os
import time
import pickle
import logging

from AnyQt.QtWidgets import QComboBox, QStyle, QSizePolicy, QFileDialog

from Orange.base import Model
from Orange.widgets import widget, gui
from Orange.widgets.settings import Setting
from Orange.widgets.utils import stdpaths
from Orange.widgets.utils.widgetpreview import WidgetPreview
from Orange.widgets.widget import OWWidget, Input, Msg

from onesaitplatform.files.filemanager import \
    FileManager

log = logging.getLogger(__name__)

CONSOLE_DEBUG = False
def printt(msg):
    if CONSOLE_DEBUG:
        print("[{}] {}".format(time.ctime(), msg))

class OspUploadModel(widget.OWWidget):
    name = "Upload Model"
    description = "Upload a trained model to a binary file in the host platform."
    icon = "icons/file_manager_upload.svg"

    class Inputs:
        model = Input("Model", Model)

    file_manager = None
    host = Setting("www.onesaitplatform.online")
    user_token = Setting(None)
    filename = Setting(None)
    
    FILE_EXT = '.pkcls'
    FILTER = "Pickled model (*" + FILE_EXT + ");;All Files (*)"
    TEMPORAL_FOLDER = os.path.join(os.path.dirname(__file__), "tmp")

    want_main_area = False
    #resizing_enabled = False

    _valid_arguments = True

    class Error(OWWidget.Error):
        custom = Msg("{}")

    class Information(OWWidget.Information):
        custom = Msg("{}")

    def __init__(self):
        printt("Init {} widget".format(__class__)) 
        log.info("Init {} widget".format(__class__))
        super().__init__()

        self.file_manager = None
        self.model = None
        self.user_token = "Bearer ..."
        self.filename = None

        self.__init__gui()
        self._init_file_manager()

        

    def __init__gui(self):
        self.box_parameters = gui.widgetBox(self.controlArea, "Uploading parameters")

        # host     
        gui.lineEdit(self.box_parameters, self, "host", "Enter host",
                    callback=self.host_changed,
                    valueType=str)

        # user_token
        gui.lineEdit(self.box_parameters, self, "user_token", "Enter user Token",
                    callback=self.user_token_changed,
                    valueType=str)

        # filename
        gui.lineEdit(self.box_parameters, self, "filename", "Enter file name",
                    callback=self.filename_changed,
                    valueType=str)
        
        #button
        self.uploadbutton = gui.button(
            self.controlArea, self, "Upload", callback=self.upload_file,
            default=True
        )
        self.uploadbutton.setEnabled(False)


    def _init_file_manager(self):
        printt("Init {} file manager".format(__class__))
        log.info("Init {} file manager".format(__class__))
        if self.all_arguments_ok(): 
            self.file_manager = FileManager(host=self.host, user_token=self.user_token)
        printt("Created file_manager {}".format(self.file_manager)) 
        log.info("Created file_manager {}".format(self.file_manager)) 

    
    @Inputs.model
    def setModel(self, model):
        """Set input model."""
        self.model = model
        self.uploadbutton.setEnabled(
            not (model is None or self.filename is None))

    def host_changed(self):
        self.validate_arguments()
        if self.host != self.file_manager.host:
            printt("Setting new host: {}".format(self.host))
            log.info("Setting new host: {}".format(self.host))
            self.file_manager.host = self.host

        self.uploadbutton.setEnabled(self._valid_arguments)

    def user_token_changed(self):
        self.validate_arguments()
        if self.user_token != self.file_manager.user_token:
            printt("Setting new user_token: {}".format(self.user_token))
            log.info("Setting new user_token: {}".format(self.user_token))
            self.file_manager.user_token = self.user_token

        self.uploadbutton.setEnabled(self._valid_arguments)

    def filename_changed(self):
        self.validate_arguments()
        self.uploadbutton.setEnabled(self._valid_arguments)

    def validate_arguments(self):
        printt("Validating arguments: {} - {} - {}".format(self.host, self.user_token, self.filename))
        log.info("Validating arguments: {} - {} - {}".format(self.host, self.user_token, self.filename))
        self._valid_arguments = True
        incorrects = []
        
        if self.host in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("host")

        if self.user_token in [" ", "", "-", ".", "None", None] :
            self._valid_arguments = False
            incorrects.append("user_token")
        elif len(self.user_token) < 25:
            self._valid_arguments = False
            incorrects.append("user_token")
        
        if self.filename in [" ", "", "-", ".", "None", None]:
            self._valid_arguments = False
            incorrects.append("filename")

        if len(incorrects) > 0:
            self.Error.custom("Incorrect arguments: {}".format(", ".join(incorrects)))
            log.error("Incorrect arguments: {}".format(", ".join(incorrects)))

        else:
            self.Error.custom.clear()

    def all_arguments_ok(self):
        if self._valid_arguments:
            printt("Valid arguments: {} - {} - {}".format(self.host, self.user_token, self.filename))
            log.info("Valid arguments: {} - {} - {}".format(self.host, self.user_token, self.filename))
        else:
            printt("Not valid arguments: {} - {} - {}".format(self.host, self.user_token, self.filename))
            log.warn("Not valid arguments: {} - {} - {}".format(self.host, self.user_token, self.filename))
        
        return self._valid_arguments

    def save(self, filename):
        """Save the model to filename (model must not be None)."""
        _saved = False
        assert self.model is not None
        try:
            with open(filename, "wb") as f:
                pickle.dump(self.model, f)
            _saved = True
        except pickle.PicklingError:
            raise
        except os.error:
            raise
        else:
            printt("Not possible to upload model")
            log.warn("Not possible to upload model")
        return _saved

    def upload(self, filename, tmp_file_path):
        return self.file_manager.upload_file(filename=filename, filepath=tmp_file_path)

    def upload_file(self):
        """
        Save the model to current selected filename.
        Do nothing if model or current filename are None.
        """
        self.Information.custom.clear()
        if self.model is not None and self.all_arguments_ok():
            if not self.filename.endswith(self.FILE_EXT):
                self.filename += self.FILE_EXT
            tmp_file = os.path.join(self.TEMPORAL_FOLDER, self.filename)

            self.save(tmp_file)
            up_ok, up_res = self.upload(self.filename, tmp_file)
            self.remove_tmp_file(tmp_file)

            if up_ok:
                self.Information.custom("Uploaded file! id: {}".format(up_res["id"]))
                self.filename = None
                self.uploadbutton.setEnabled(False)

            else:
                self.Error.custom("Not possible to upload file: {}".format(up_res))

    def remove_tmp_file(self, tmp_file_path):
        if os.path.exists(tmp_file_path):
            try:
                os.remove(tmp_file_path)
            except:
                pass



if __name__ == "__main__":  # pragma: no cover
    WidgetPreview(OspUploadModel).run()