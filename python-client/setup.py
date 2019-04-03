#!/usr/bin/env python

from os import path, walk
import sys
from setuptools import setup, find_packages

NAME = "onesaitplatform-services"

VERSION = "0.0.2"

DESCRIPTION = "Python Implementation of the Onesait Platform utilities"
LONG_DESCRIPTION = open(path.join(path.dirname(__file__), 'README.md')).read()

LICENSE = "BSD"

KEYWORDS = (
    'onesaitplatform services',
)

PACKAGES = find_packages()

NAMESPACE_PACKAGES = ["onesaitplatform"]


if __name__ == '__main__':

    setup(
        name=NAME,
        version=VERSION,
        description=DESCRIPTION,
        long_description=LONG_DESCRIPTION,
        license=LICENSE,
        packages=PACKAGES,
        keywords=KEYWORDS,
        namespace_packages=NAMESPACE_PACKAGES,
        include_package_data=True,
        zip_safe=False,
    )
