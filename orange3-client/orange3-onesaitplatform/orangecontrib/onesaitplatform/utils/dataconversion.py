import json
from collections import OrderedDict

import numpy as np 
import pandas as pd

from Orange.data import ContinuousVariable, DiscreteVariable, StringVariable, Domain, Table


def json_data_to_pandas_dataframe(json_data):
    if len(json_data) <= 0 or json_data is None:
        return None

    data_0 = json_data[0]
    if type(data_0) == str:
        json_data = [json.loads(d) for d in json_data]

    return pd.io.json.json_normalize(json_data)

def json_data_to_orange_table(json_data):
    pandas_dataframe = json_data_to_pandas_dataframe(json_data)
    return pandas_dataframe_to_orange_table(pandas_dataframe)

def pandas_dataframe_to_orange_table(df):
    dom, attrib, metas = create_domain_from_dataframe(df)
    orange_table = Table.from_numpy(domain = dom, X = df[attrib].values, Y = None, 
                                    metas = df[metas].values, W = None)
    return orange_table

def create_domain_from_dataframe(dataframe):
    columns = OrderedDict(dataframe.dtypes)
    attributes = OrderedDict()
    metas = OrderedDict()
    
    for name, dtype in columns.items():
        if issubclass(dtype.type, np.number):
            if len(dataframe[name].unique()) >= 13 or issubclass(dtype.type, np.inexact) or (dataframe[name].max() > len(dataframe[name].unique())):
                attributes[name] = ContinuousVariable(name)
            else:
                dataframe[name] = dataframe[name].astype(str)
                attributes[name] = DiscreteVariable(name, values = sorted(dataframe[name].unique().tolist()))
        else:
            metas[name] = StringVariable(name)

    dom = Domain(attributes = attributes.values(), metas = metas.values())
    attrib = list(attributes.keys())
    metas = list(metas.keys())

    return dom, attrib, metas
