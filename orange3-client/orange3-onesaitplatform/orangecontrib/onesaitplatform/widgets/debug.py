from Orange.widgets.widget import OWWidget, Input
from Orange.widgets import gui

class Debug(OWWidget):
    name = "Debug"
    description = "Print out text"
    icon = "icons/print.svg"

    class Inputs:
        obj = Input("Debug:", object)

    want_main_area = False

    def __init__(self):
        super().__init__()
        self.obj = None

        self.label = gui.widgetLabel(self.controlArea, "Debug: ")

    @Inputs.obj
    def set_obj(self, obj):
        """Set the input obj."""
        self.obj = obj
        if self.obj is None:
            self.label.setText("Debug: None")
        else:
            self.label.setText("Debug: {}".format(self.obj))