# BaseModelService example
## Define a model service class that inherits from BaseModelService
```python
from onesaitplatform.model import BaseModelService
```

```python
class MyModelService(BaseModelService):
    """Specific definition of a subtype of BaseModelService"""

    def __init__(self, **kargs):
        """

        YOUR CODE HERE
        
        You may want to initialize here some object attributes that will be used later.
        Typically, you will create a 'model' attribute. Later, inside the 'load_model' method,
        you will save your model in this 'model' attribute. And, in the 'predict' method, you will
        invoke this 'model' attribute to infer according to your model.

        """

        super().__init__(**kargs)

    def load_model(self, model_path=None, hyperparameters=None, extra_path=None):
        """Loads a previously trained model and save it a one or more object attributes"""

        """

        YOUR CODE HERE

        'model_path': is a local path to a folder that contains the files or
        directories that are needed to load the model. You will use its content to load the model

        'hyperparameters': is a dictionary with the hyperparameters that you may
        need to build the model. Those are the hyperparameters that were provided previously
        in the training process.

        'extra_path': is a local path to a folder that contains other files or
        directories needed to load the model, resources esplicitally provided by the user when the
        training process is launched

        Once you have loaded the model, you will save it in one or more attributes of the
        object (i.e. 'model'), from where you will invoke the model to infer in the 'predict' method

        """


    def train(self, dataset_path=None, hyperparameters=None, model_path=None, extra_path=None, pretraind_path=None):
        """
        Trains a model given a dataset and saves it in a local path.
        Returns a dictionary with the obtained metrics
        """

        """

        YOUR CODE HERE

        'dataset_path': is a local path to a file that contains the training dataset. This is just the
        file that was saved in the Onesait Platform File Repository. If the dataset is a Onesait Platform
        Ontology, the file received here is a CSV where the delimiter is ',' and the columns are the fields
        of the ontology.

        'hyperparameters': is a dictionary with the hyperparameters provided by the user.

        'model_path': is a path to a local directory. Once the model is trained, it must be saved inside
        this directory.

        'extra_path': is a local path to a folder that contains other files or
        directories needed to train and load the model, resources esplicitally provided by the user when the
        training process is launched.

        'pretrained_path': is a local path to a folder that contains all the files or
        directories needed to load a previously trained version of the model. It can be used to fine-tune
        a new version of the model.

        The dataset for training must be opened as a file in the expected format by means of the 'dataset_path'.
        The provided hyperparameters must be taken from the 'hyperparameters' dictionary. You must train a model
        with that dataset and those hyperparameter. Once it has been trained, you must save it in 'model_path'.
        And you must evaluate it and return a dictionary with the obtained metrics.

        """

        return metrics

    def predict(self, inputs=None):
        """Predicts given a model and an array of inputs"""

        """

        YOUR CODE HERE

        inputs: is an array with the 'n' items that must be used to infer
        
        The model that will be used to infer must have been previously saved in one or more objet
        attributes, in the 'load_model' method. You must use that model to infer results for the
        items provided in 'inputs'. Those results must be returned as an array. 

        """

        return results

```

## Create a model service object

```python
PARAMETERS = {
    'PLATFORM_HOST': "lab.onesaitplatform.com",
    'PLATFORM_PORT': 443,
    'PLATFORM_DIGITAL_CLIENT': "MyDigitalClient",
    'PLATFORM_DIGITAL_CLIENT_TOKEN': "MyDigitalClientToken",
    'PLATFORM_DIGITAL_CLIENT_PROTOCOL': "https",
    'PLATFORM_DIGITAL_CLIENT_AVOID_SSL_CERTIFICATE': True,
    'PLATFORM_ONTOLOGY_MODELS': "MyModelsOntology",
    'PLATFORM_USER_TOKEN': "Bearer MyUserToken",
    'TMP_FOLDER': '/tmp/',
    'NAME': "MyModel"
}

model_service = MyModelService(config=PARAMETERS)
```

## Predict according to the best version of the model

```python
INPUTS = [...]

results = model_service.predict(inputs=INPUTS)
```

## Train a new version of the model from dataset in File Repository

```python

MODEL_NAME = 'MyModel'
MODEL_VERSION = '0'
MODEL_DESCRIPTION = 'First version of MyModel'
DATASET_FILE_ID = 'FileIDInOnesaitPlatformFileRepository'
HYPERPARAMETERS = {
    # This is just an example
    'BATCH_SIZE': 16,
    'EPOCHS': 10,
    'DROPOUT': 0.2,
    'LEARNING_RATE': 0.001,
    'TRAIN_PERCENT': 0.90,
    ...
}

model_service.train_from_file_system(
    name=MODEL_NAME, version=MODEL_VERSION, description=MODEL_DESCRIPTION,
    dataset_file_id=DATASET_FILE_ID, hyperparameters=HYPERPARAMETERS
)
```

Optionally, can also be provided two more attributes: extra_file_id and pretrained_model_id.

```python

...

EXTRA_FILE_ID = '...'
PRETRAINED_MODEL_ID = '...'

model_service.train_from_file_system(
    name=MODEL_NAME, version=MODEL_VERSION, description=MODEL_DESCRIPTION,
    dataset_file_id=DATASET_FILE_ID, hyperparameters=HYPERPARAMETERS,
    extra_file_id=EXTRA_FILE_ID, pretrained_model_id=PRETRAINED_MODEL_ID
)
```

## Train a new version of the model from dataset in Ontology

```python

MODEL_NAME = 'MyModel'
MODEL_VERSION = '0'
MODEL_DESCRIPTION = 'First version of MyModel'
ONTOLOGY_DATASET = 'OnesaitPlatformOntology'
HYPERPARAMETERS = {
    # This is just an example
    'BATCH_SIZE': 16,
    'EPOCHS': 10,
    'DROPOUT': 0.2,
    'LEARNING_RATE': 0.001,
    'TRAIN_PERCENT': 0.90,
    ...
}

model_service.train_from_ontology(
    name=MODEL_NAME, version=MODEL_VERSION, description=MODEL_DESCRIPTION,
    ontology_dataset=ONTOLOGY_DATASET, hyperparameters=HYPERPARAMETERS
)
```

Optionally, can also be provided two more attributes: extra_file_id and pretrained_model_id.

```python

...

EXTRA_FILE_ID = '...'
PRETRAINED_MODEL_ID = '...'

model_service.train_from_ontology(
    name=MODEL_NAME, version=MODEL_VERSION, description=MODEL_DESCRIPTION,
    ontology_dataset=ONTOLOGY_DATASET, hyperparameters=HYPERPARAMETERS,
    extra_file_id=EXTRA_FILE_ID, pretrained_model_id=PRETRAINED_MODEL_ID
)
```

## Predict from dataset in File Repository

```python

DATASET_FILE_ID = 'FileIDInOnesaitPlatformFileRepository'

results = model_service.predict_from_file_system(
    dataset_file_id=DATASET_FILE_ID
)
```

The output can also be saved in a new Ontology

```python

DATASET_FILE_ID = 'FileIDInOnesaitPlatformFileRepository'
OUTPUT_ONTOLOGY = ' MyOutputOntology'

model_service.predict_from_file_system(
    dataset_file_id=DATASET_FILE_ID, output_ontology=OUTPUT_ONTOLOGY
)
```

## Predict from dataset in Ontology

```python

INPUT_ONTOLOGY = 'MyInputOntology'

results = model_service.predict_from_ontology(
    input_ontology=INPUT_ONTOLOGY
)
```

The output can also be saved in a new Ontology

```python

INPUT_ONTOLOGY = 'MyInputOntology'
OUTPUT_ONTOLOGY = ' MyOutputOntology'

model_service.predict_from_ontology(
    dataset_file_id=DATASET_FILE_ID, output_ontology=OUTPUT_ONTOLOGY
)
```

## Reload the active model

```python

model_service.reload()

```

## Report events in Platform audit ontology

```python

MESSAGE = '...'
RESULT = 'SUCCESS|ERROR|WARNING'

model_service.report(
    message=MESSAGE, result=RESULT
)

```
