__all__ = ['voie_controller', 'get_voies', 'set_voie_interdite',
           'create_voie', 'delete_voie']

from flask import Blueprint, request, make_response
from werkzeug.exceptions import BadRequest

from model.production.Voie import Voie
from services import voieServices

voie_controller = Blueprint('voie', __name__)


def build_voie(voie: Voie):
    return dict(numVoie=voie.num_voie, interdite=voie.interdite)


@voie_controller.route("/api/v1/voies", methods=['GET'])
def get_voies():
    voies = voieServices.get_voies()

    return [build_voie(voie) for voie in voies]


@voie_controller.route("/api/v1/voies", methods=['POST'])
def create_voie():
    data = request.get_json(force=False)
    num_voie = data.get('numVoie')
    interdite = data.get('interdite', False)
    voie = voieServices.create_voie(num_voie, interdite)
    return build_voie(voie)


@voie_controller.route("/api/v1/voies/<num_voie>", methods=['GET'])
def get_voie(num_voie: str):
    return build_voie(voieServices.get_voie(int(num_voie)))


@voie_controller.route("/api/v1/voies/<num_voie>", methods=['DELETE'])
def delete_voie(num_voie: str):
    voieServices.delete_voie(int(num_voie))
    return make_response('', 204)


@voie_controller.route("/api/v1/voies/<num_voie>/interdite", methods=['PUT'])
def set_voie_interdite(num_voie: str):
    data = request.get_json(force=False)
    interdite = data.get('interdite')
    if interdite is None:
        raise BadRequest('Information manquante: interdite')
    interdite = voieServices.disable_voie(int(num_voie), interdite)
    return dict(numVoie=num_voie, interdite=interdite)
