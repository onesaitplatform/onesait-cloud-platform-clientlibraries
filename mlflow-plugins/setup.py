from setuptools import setup
import sys
import os
sys.path.insert(0, './onesaitplatformplugins')

setup(
    name="mlflow-onesaitplatform-plugin",
    version="0.2.11",
    description="Plugins for MLflow and Onesait Platform",
    packages=['onesaitplatformplugins'],
    install_requires=["mlflow", "onesaitplatform-client-services"],
    entry_points={
        # Define a ArtifactRepository plugin for artifact URIs with scheme 'onesait-platform'
        "mlflow.artifact_repository": "onesait-platform=onesaitplatformplugins.plugins:OnesaitPlatformArtifactRepository"
    },
    long_description=open(os.path.join(os.path.dirname(__file__), 'README.md')).read(),
    long_description_content_type="text/markdown",
    license="Apache v2.0",
    url="",
    keywords=('onesaitplatform mlflow plugin'),
    namespace_packages=["onesaitplatformplugins"],
    include_package_data=True,
    zip_safe=False,
)
