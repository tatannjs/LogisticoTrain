import logging
from typing import Dict

from flask import Flask
import mongoengine as me
# from flask_mongoengine import MongoEngine

from utils.Singleton import Singleton

LOG = logging.getLogger(__name__)

__all__ = ['HistoryDbDAO']


class HistoryDbDAO(metaclass=Singleton):
    __slots__ = ['__db', '__connect_params', '__connected']

    def __init__(self, configuration: Dict = None):
        self.__build_connect_params(configuration)
        self.__connected = False

    def __build_connect_params(self, configuration: Dict):
        # retrieve related configuration values
        db_conf = configuration['MONGODB_SETTINGS']
        db_name = db_conf.get('db', 'test')
        host = db_conf.get('host', 'localhost')
        port = db_conf.get('port', 27017)
        username = db_conf.get('username')
        password = db_conf.get('password')
        auth_source = db_conf.get('authentication_source')

        # build params
        connect_params = dict(db=db_name, host=host, port=port, alias='default')
        if (username is not None):
            connect_params['username'] = username
            if (password is not None):
                connect_params['password'] = password
        if (auth_source is not None):
            connect_params['authentication_source'] = auth_source

        self.__connect_params = connect_params
        LOG.info("Mongo connection param set: " + str(self.__connect_params))

    def open(self) -> None:
        if self.__connected:
            self.close()
        me.connect(**self.__connect_params)
        self.__connected = True

    def close(self) -> None:
        if not self.__connected:
            return
        me.disconnect()
        self.__connected = False

    def __enter__(self):
        self.open()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        try:
            self.close()
        except Exception as e:
            LOG.warning("Exception while closing Mongo connection: " + str(e))