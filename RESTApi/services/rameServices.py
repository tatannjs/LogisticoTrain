import logging
from typing import List, Tuple, Optional, Any

from sqlalchemy import select, delete, and_, func, Exists
from werkzeug.exceptions import BadRequest, NotFound

from dao.production_db import ProductionDbDAO
from model.history.HistoryAction import create_hist_ajout_tache, create_hist_sortie, create_hist_real_taches, \
    HistoryAction
from model.production.Rame import Rame
from model.production.Tache import Tache

LOG = logging.getLogger(__name__)

__all__ = ['get_rames', 'get_rames_on_voies', 'get_removable_rames', 'get_rame_info', 'terminate_taches', 'add_taches', 'remove_rame']


def get_rames() -> Any:
    prod_db_session = ProductionDbDAO().session
    return prod_db_session.scalars(select(Rame)).all()


def get_rames_on_voies() -> Any:
    prod_db_session = ProductionDbDAO().session
    return prod_db_session.scalars(select(Rame).where(Rame.voie.is_not(None))).all()


def get_removable_rames() -> Any:
    prod_db_session = ProductionDbDAO().session
    exists_criteria = select(Tache.num_serie).where(Tache.num_serie == Rame.num_serie).exists()
    return prod_db_session.scalars(select(Rame).where(~exists_criteria)).all()


def get_rame_info(num_serie: str, with_taches: bool = False) -> Tuple[Rame, Optional[List[Tache]]]:
    # Validate input data
    if not num_serie or len(num_serie) > 12:
        raise BadRequest('Mauvais numéro de série de rame')

    prod_db_session = ProductionDbDAO().session
    rame: Optional[Rame] = prod_db_session.get(Rame, num_serie)
    if not rame:
        raise NotFound('Rame inconnue')
    if with_taches:
        rq_taches = select(Tache.num_tache, Tache.tache) \
            .where(Tache.num_serie == num_serie) \
            .order_by(Tache.num_tache)
        taches = list(prod_db_session.execute(rq_taches))
    else:
        taches = None
    return rame, taches


def terminate_taches(num_serie: str, taches_effectuees: List[int], auteur: str) -> Tuple[Rame, List[Tache], List[HistoryAction]]:
    # Validate input data
    if not num_serie or len(num_serie) > 12:
        raise BadRequest('Mauvais numéro de série de rame')
    if not auteur or len(auteur) > 50:
        raise BadRequest('Operateur manquant ou incorrect')
    if not taches_effectuees or any(filter(lambda l: not l, taches_effectuees)):
        raise BadRequest('Mauvaise tache effecutée')

    prod_db_session = ProductionDbDAO().session
    # Retrieve Rame
    rame: Optional[Rame] = prod_db_session.get(Rame, num_serie)
    if not rame:
        raise NotFound('Rame inconnue')

    # Retrieve taches to delete to get their content
    rq_taches = select(Tache.tache) \
        .where(and_(
        Tache.num_serie == num_serie,
        Tache.num_tache.in_(taches_effectuees)
        ))
    taches = list(map(lambda t: t[0], prod_db_session.execute(rq_taches)))

    # Delete taches
    rq_delete_taches = delete(Tache).where(
        and_(
            Tache.num_serie == num_serie,
            Tache.num_tache.in_(taches_effectuees)
        )
    )
    res = prod_db_session.execute(rq_delete_taches)
    # check that all requested taches have been deleted
    if res.rowcount != len(taches_effectuees):
        raise BadRequest('Bad task to delete')

    # Create a history action
    LOG.info("taches: %s" % str(taches))
    history_action = create_hist_real_taches(num_serie, type_rame=rame.type_rame, voie=rame.voie, auteur=auteur,
                          taches=taches)
    history_action.save()

    # Retrieve tasks left
    rq_taches = select(Tache.num_tache, Tache.tache) \
        .where(Tache.num_serie == num_serie) \
        .order_by(Tache.num_tache)
    taches_restantes = list(prod_db_session.execute(rq_taches))

    # commit session
    prod_db_session.commit()
    # return rame info
    return rame, taches_restantes, [history_action]


def add_taches(num_serie: str, taches: List[str], auteur: str) -> Tuple[Rame, List[Tache], List[HistoryAction]]:
    # Validate input data
    if not num_serie or len(num_serie) > 12:
        raise BadRequest('Mauvais numéro de série de rame')
    if not auteur or len(auteur) > 50:
        raise BadRequest('Auteur manquant ou incorrect')
    if not taches or any(filter(lambda l: not l, taches)):
        raise BadRequest('Mauvaise tache à ajouter')

    prod_db_session = ProductionDbDAO().session
    # Retrieve Rame
    rame: Optional[Rame] = prod_db_session.get(Rame, num_serie)
    if not rame:
        raise NotFound('Rame inconnue')

    # Retrieve max count of tache for the rame
    max_task_num = prod_db_session.scalar(select(func.max(Tache.num_tache)).where(Tache.num_serie == num_serie))
    if max_task_num is None:
        max_task_num = 0
    LOG.info("Max task num: " + str(max_task_num))

    # Create new taches
    new_taches = [Tache(num_serie, num_tache, tache)
                  for num_tache, tache in enumerate(taches, start=max_task_num + 1)]
    prod_db_session.add_all(new_taches)

    # Retrieve previous all tasks
    rq_taches = select(Tache) \
        .where(Tache.num_serie == num_serie) \
        .order_by(Tache.num_tache)
    taches_restantes = list(map(lambda x: x[0], prod_db_session.execute(rq_taches)))

    #merge previsoou and new taches
    all_taches = taches_restantes + new_taches

    # Create a history action
    history_action = create_hist_ajout_tache(num_serie, rame.type_rame, rame.voie, auteur, taches)
    history_action.save()

    # commit session
    prod_db_session.commit()
    # return rame info
    return rame, all_taches, [history_action]


def remove_rame(num_serie: str, auteur: str) -> None:
    if not num_serie or len(num_serie) > 12:
        raise BadRequest('Mauvais numéro de série de rame')
    if not auteur or len(auteur) > 50:
        raise BadRequest('Operateur manquant ou incorrect')

    prod_db_session = ProductionDbDAO().session

    # Retrieve Rame
    rame: Optional[Rame] = prod_db_session.get(Rame, num_serie)
    if not rame:
        raise NotFound('Rame inconnue')

    # Remove Rame -> will raise exeption if tasks are remaining
    rq = delete(Rame).where(Rame.num_serie == num_serie)
    LOG.info(rq)
    res = prod_db_session.execute(rq)

    # Create a history action
    history_action = create_hist_sortie(num_serie, rame.type_rame, rame.voie, auteur)
    history_action.save()

    # commit session
    prod_db_session.commit()


# def get_rame_actions(num_serie: str) -> Iterable[HistoryAction]:
#     # Get Rame history actions, sorted by timestamp desc
#     return HistoryAction.objects(num_serie=num_serie).order_by('-timestamp')
