#!/usr/bin/env python

from os import path, walk
import sys
from setuptools import setup, find_packages

NAME = "onesaitplatform-client-services"

VERSION = "1.0.2"

DESCRIPTION = "Python Implementation of the Onesait Platform utilities"
LONG_DESCRIPTION = open(path.join(path.dirname(__file__), 'README.md')).read()

LICENSE = "BSD"

KEYWORDS = (
    'onesaitplatform client services',
)

PACKAGES = find_packages()

REQUIRED_PACKAGES = ["paho-mqtt", "six", "requests"]

NAMESPACE_PACKAGES = ["onesaitplatform"]


if __name__ == '__main__':

    setup(
        name=NAME,
        version=VERSION,
        description=DESCRIPTION,
        long_description=LONG_DESCRIPTION,
        license=LICENSE,
        packages=PACKAGES,
        url="https://onesait-git.cwbyminsait.com/onesait-platform/onesait-cloud-platform-clientlibraries.git",
        install_requires=REQUIRED_PACKAGES,
        keywords=KEYWORDS,
        namespace_packages=NAMESPACE_PACKAGES,
        include_package_data=True,
        zip_safe=False,
    )
