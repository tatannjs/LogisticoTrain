from typing import Dict, List, Optional

from flask import Blueprint, request, make_response
from werkzeug.exceptions import BadRequest

from controllers.actionsController import build_action
from model.production.Rame import Rame
from model.production.Tache import Tache
from services import rameServices, actionServices

__all__ = ['rame_controller']

rame_controller = Blueprint('rame', __name__)


def _build_rame_with_taches_representation(rame: Rame, taches: Optional[List[Tache]] = None) -> Dict:
    dic_rep = dict(numSerie=rame.num_serie, typeRame=rame.type_rame, voie=rame.voie,
                   conducteurEntrant=rame.conducteur_entrant)
    if taches is not None:
        dic_rep['taches'] = [dict(idx=t.num_tache, tache=t.tache) for t in taches]
    return dic_rep


@rame_controller.route("/api/v1/rames", methods=['GET'])
def get_rames():
    removable_only = request.args.get('removable')
    on_voie_only = request.args.get('on-voie')
    if removable_only is not None:
        rames = rameServices.get_removable_rames()
    elif on_voie_only is not None:
        rames = rameServices.get_rames_on_voies()
    else:
        rames = rameServices.get_rames()
    return [_build_rame_with_taches_representation(r) for r in rames]


@rame_controller.route("/api/v1/rames/<num_serie>", methods=['GET'])
def get_rame(num_serie: str):
    with_details = True if 'details' in request.args else False
    rame, taches = rameServices.get_rame_info(num_serie, with_details)
    return _build_rame_with_taches_representation(rame, taches)


@rame_controller.route("/api/v1/rames/<num_serie>/actions", methods=['GET'])
def get_rame_actions(num_serie: str):
    return [build_action(action) for action in actionServices.get_actions(num_serie)]


@rame_controller.route("/api/v1/rames/<num_serie>/actions", methods=['POST'])
def create_rame_action(num_serie: str):
    data = request.get_json(force=False)
    action = data.get('action')
    auteur = data.get('auteur')
    if not action or not auteur:
        raise BadRequest('Information manquante: action et auteur sont obligatoires')

    if action == 'realTaches':
        taches_effectuees = data.get('taches')
        if not taches_effectuees:
            raise BadRequest('Information manquante: taches est obligatoire')
        rame, taches, actions = rameServices.terminate_taches(num_serie, taches_effectuees, auteur)
        res = _build_rame_with_taches_representation(rame, taches)
        res['actions'] = [build_action(action) for action in actions]
        return res

    elif action == 'ajoutTaches':
        taches_to_create = data.get('taches')
        if not taches_to_create:
            raise BadRequest('Information manquante: taches est obligatoire')
        rame, taches, actions = rameServices.add_taches(num_serie, taches_to_create, auteur)
        res = _build_rame_with_taches_representation(rame, taches)
        res['actions'] = [build_action(action) for action in actions]
        return res

    else:
        raise BadRequest('Action inconnue')
