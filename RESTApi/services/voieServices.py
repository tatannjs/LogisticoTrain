import logging
from typing import Any, Optional

from sqlalchemy import select, delete
from sqlalchemy.orm.sync import update
from werkzeug.exceptions import BadRequest, NotFound

from dao.production_db import ProductionDbDAO
from model.production.Voie import Voie

LOG = logging.getLogger(__name__)

__all__ = ['get_voies', 'disable_voie', 'create_voie', 'delete_voie']


def get_voies() -> Any:
    prod_db_session = ProductionDbDAO().session
    voies = prod_db_session.scalars(select(Voie)).all()
    LOG.info(str(voies))
    return voies

def get_voie(num_voie: int) -> Voie:
    if not num_voie:
        raise BadRequest('Mauvais numéro de voie')

    prod_db_session = ProductionDbDAO().session
    voie: Optional[Voie] = prod_db_session.get(Voie, num_voie)
    if not voie:
        raise NotFound('Voie inconnue')
    return voie


def disable_voie(num_voie: int, disabled: bool = False) -> bool:
    if not num_voie:
        raise BadRequest('Mauvais numéro de voie')

    prod_db_session = ProductionDbDAO().session
    # Retrieve voie
    voie: Optional[Voie] = prod_db_session.get(Voie, num_voie)
    if not voie:
        raise NotFound('Voie inconnue')
    # Update disabled field of voie
    LOG.info("Update voie with num_voie %d" % num_voie)
    voie.interdite = disabled
    #rq_update = update(Voie).where(Voie.num_voie == num_voie).values(interdite=disabled)
    #res = prod_db_session.execute(rq_update)
    # Check one voie has been updated
    #LOG.info("Check row count")
    #if res.rowcount == 0:
    #    raise NotFound('Voie inconnue')
    # commit session
    LOG.info("Commit")
    prod_db_session.commit()
    LOG.info("return")
    return disabled


def create_voie(num_voie: int, disabled: bool = False) -> Voie:
    if not num_voie:
        raise BadRequest('Mauvais numéro de voie')

    prod_db_session = ProductionDbDAO().session
    # Create new voie
    voie = Voie(num_voie, disabled)
    prod_db_session.add(voie)
    # Commit session
    prod_db_session.commit()
    return voie


def delete_voie(num_voie: int) -> Voie:
    if not num_voie:
        raise BadRequest('Mauvais numéro de voie')

    prod_db_session = ProductionDbDAO().session
    # Remove Voie -> will raise ex if a rame is ine the voie
    rq = delete(Voie).where(Voie.num_voie == num_voie)
    LOG.info(rq)
    res = prod_db_session.execute(rq)
    # Check one voie has been removed
    if res.rowcount == 0:
        raise NotFound('Voie inconnue')
    # commit session
    prod_db_session.commit()