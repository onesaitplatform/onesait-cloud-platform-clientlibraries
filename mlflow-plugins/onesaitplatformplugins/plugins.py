import re
import os
import json
import mlflow
import logging

from mlflow.entities import FileInfo
from onesaitplatform.files import FileManager
from mlflow.store.artifact.artifact_repo import ArtifactRepository

ARTIFACTS_PARAM_KEY = '__artifacts__'

_logger = logging.getLogger(__name__)

class OnesaitPlatformArtifactRepository(ArtifactRepository):
    """OnesaitPlatformArtifactRepository provided through plugin system"""

    def __init__(self, *args, **kwargs):
        """Initialization of the object, given a config file for OSP deployment"""
        super(OnesaitPlatformArtifactRepository, self).__init__(*args, **kwargs)

        config = self.parse_artifact_uri(
            self.artifact_uri
        )

        if config['host'] is None or config['token'] is None:
            raise ValueError('No host or token provided in artifact uri {}'.format(self.artifact_uri))

        self.osp_file_manager = FileManager(
            host=config['host'], user_token=config['token']
            )
        self.osp_file_manager.protocol = "https"

    def parse_artifact_uri(self, artifact_uri):
        """Returns parameters from artifact uri"""
        match = re.match(
            'onesait-platform:[/]{2}([^@]+)@([^/]+)(?:[/]([0-9]+)[/]([^/]+)[/]artifacts(?:[/](.+))?)?',
            artifact_uri
            )
        if not match:
            raise ValueError('Unable to parse artifact uri {}'.format(artifact_uri))

        token = match.group(1)
        host = match.group(2)
        experiment_id = match.group(3)
        run_id = match.group(4)
        path = match.group(5)

        return {
            'token': token,
            'host': host,
            'experiment_id': experiment_id,
            'run_id': run_id,
            'path': path
            }

    def get_artifacts_info(self, artifact_uri):
        """Returns run info according to tracking server"""
        info = self.parse_artifact_uri(artifact_uri)
        run_id = info['run_id']
        experiment_id = info['experiment_id']

        if run_id is None or experiment_id is None:
            raise ValueError('No run or experiment provided in artifact uri {}'.format(artifact_uri))

        runs_in_experiment = mlflow.search_runs([experiment_id])
        run = runs_in_experiment[runs_in_experiment['run_id'] == run_id]

        if not len(run):
            raise AttributeError('No run found for this uri {}'.format(
            self.artifact_uri
            ))

        artifacts_info, artifact_index = [], 1
        while artifact_index:
            parameter_key = 'params.{}{}'.format(
                ARTIFACTS_PARAM_KEY, str(artifact_index)
                )
            if parameter_key in run.columns and\
               run[parameter_key].tolist()[0] is not None:
                artifact_info = run[parameter_key].tolist()[0]
                try:
                    artifact_info = json.loads(artifact_info)
                except ValueError:
                    raise ValueError('Unable to parse artifacts info: {}'.format(artifact_info))
                artifacts_info.append(artifact_info)
                artifact_index += 1
            else:
                artifact_index = None

        return artifacts_info

    def upload_artifact(self, local_path, artifact_name):
        """Upload artifact to OSP File Repository"""
        uploaded, info = self.osp_file_manager.upload_file(
            artifact_name, local_path
            )
        uploaded_artifact_id = None
        if not uploaded:
            file_manager_info = self.osp_file_manager.to_json()
            raise ConnectionError(
        "Not possible to upload artifact with file manager: {}".format(
            file_manager_info
            ))
        uploaded_artifact_id = info['id']
        _logger.info('Uploaded artifact: {}'.format(info))
        return uploaded_artifact_id

    def download_artifact(self, local_path, artifact_id):
        """Downloads artifact from OSP file repository"""
        downloaded, info = self.osp_file_manager.download_file(
            artifact_id, filepath=os.path.dirname(local_path)
            )
        if not downloaded:
            file_manager_info = self.osp_file_manager.to_json()
            raise ConnectionError(
        "Not possible to download artifact with file manager: {}".format(
            file_manager_info))
        _logger.info('Downloaded artifact: {}'.format(info))


    def log_artifact(self, local_file, artifact_path=None):
        """Logs a local file as an artifact in OSP repository"""
        file_size = os.path.getsize(local_file)
        remote_file = os.path.basename(local_file)
        if artifact_path:
            remote_file = '/'.join([artifact_path, remote_file])
        uploaded_artifact_id = self.upload_artifact(
            local_file, os.path.basename(local_file)
            )
        parameter_key = ARTIFACTS_PARAM_KEY + str(1)
        parameter_value = json.dumps([remote_file, uploaded_artifact_id, file_size])
        mlflow.log_param(parameter_key, parameter_value)

    def log_artifacts(self, local_dir, artifact_path=None):
        """Saves artifacts in OSP repository"""
        artifact_counter = 0
        for root, _, files in os.walk(local_dir):
            for _file in files:
                local_path = os.path.join(root, _file)
                file_size = os.path.getsize(local_path)
                remote_path = local_path[len(local_dir) + 1:]
                if artifact_path:
                    remote_path = '/'.join([artifact_path, remote_path])
                uploaded_artifact_id = self.upload_artifact(local_path, _file)
                artifact_counter += 1
                parameter_key = ARTIFACTS_PARAM_KEY + str(artifact_counter)
                parameter_value = json.dumps([remote_path, uploaded_artifact_id, file_size])
                mlflow.log_param(parameter_key, parameter_value)

    def list_artifacts(self, path=None):
        """Returns saved artifacts for current artifact uri"""
        uri = self.parse_artifact_uri(self.artifact_uri)
        root_path = uri['path']
        if (not path) and root_path:
            path = root_path
        if path and root_path:
            path = '/'.join([root_path, path])

        artifacts_info = self.get_artifacts_info(self.artifact_uri)
        artifacts_under_path_info = artifacts_info
        if path:
            artifacts_under_path_info = list(filter(
                lambda a: re.match(rf"{path}[/].+", a[0]), artifacts_info
            ))

        already_seen_paths, file_infos = [], []
        path_len = 0 if not path else (len(path) + 1)

        for artifact_under_path in artifacts_under_path_info:
            file_size = artifact_under_path[2]
            relative_path = artifact_under_path[0][path_len:]
            relative_path_steps = relative_path.split('/')
            next_step = relative_path_steps[0]
            if next_step in already_seen_paths:
                continue
            already_seen_paths.append(next_step)
            file_info_path = next_step if not path else '/'.join([path, next_step])
            if len(relative_path_steps) == 1:
                file_infos.append(FileInfo(file_info_path, False, file_size))
            else:
                file_infos.append(FileInfo(file_info_path, True, None))

        return file_infos

    def _download_file(self, remote_file_path, local_path):
        """Downloads artifact from OSP file repository"""
        uri = self.parse_artifact_uri(self.artifact_uri)
        root_path = uri['path']
        if root_path and not remote_file_path.startswith(root_path + '/'):
            remote_file_path = '/'.join([root_path, remote_file_path])

        artifacts_info = self.get_artifacts_info(
            self.artifact_uri
            )
        artifacts_info = list(filter(lambda a: a[0] == remote_file_path, artifacts_info))
        if len(artifacts_info) < 1:
            raise AttributeError('Not available artifact to download {}'.format(remote_file_path))
        elif len(artifacts_info) > 1:
            raise AttributeError('Ambiguous artefact to download {}'.format(remote_file_path))

        artifact_info = artifacts_info[0]
        osp_artifact_id = artifact_info[1]
        self.download_artifact(local_path, osp_artifact_id)
