from datetime import datetime as dt
from typing import List, Tuple

import mongoengine as me

__all__ = ['HistoryAction', 'ACTION_TYPE',
           'create_hist_real_taches', 'create_hist_ajout_tache',
           'create_hist_sortie']

ACTION_TYPE = ['demande', 'entree', 'rejet', 'realTaches', 'ajoutTaches', 'sortie']


class HistoryAction(me.Document):
    num_serie = me.StringField(db_field='numSerie', required=True, max_length=12)
    type_rame = me.StringField(db_field='typeRame', required=True, max_length=50)
    action = me.StringField(required=True, choices=ACTION_TYPE)
    auteur = me.StringField(required=True, max_length=50)
    timestamp = me.DateTimeField(default=dt.utcnow)
    voie = me.IntField(required=False)
    taches = me.ListField(me.StringField())
    _class = me.StringField(db_field='_class', required=True)

    meta = {
        'collection': 'actions',
        'indexes': [
            {
                'fields': ['auteur'],
                'unique': False,
                'name': 'auteur'
            }
        ]
    }


# def create_hist_entree(num_serie: str, type_rame: str, operateur: str,
#                        taches: List[Tuple[int, str]]) -> HistoryAction:
#     return HistoryAction(
#         num_serie=num_serie,
#         type_rame=type_rame,
#         operateur=operateur,
#         action='entree',
#         taches=[HistoryTache(idx=idx, tache=tache) for idx, tache in taches]
#     )


def create_hist_real_taches(num_serie: str, type_rame: str, voie: int, auteur: str,
                          taches: List[str]) -> HistoryAction:
    return HistoryAction(
        num_serie=num_serie,
        type_rame=type_rame,
        auteur=auteur,
        action='realTaches',
        voie=voie,
        taches=taches,
        _class='rtApi.model.HistoryAction'
    )


def create_hist_ajout_tache(num_serie: str, type_rame: str, voie: int, auteur: str,
                            taches: List[str]) -> HistoryAction:
    return HistoryAction(
        num_serie=num_serie,
        type_rame=type_rame,
        auteur=auteur,
        action='ajoutTaches',
        voie=voie,
        taches=taches,
        _class='rtApi.model.HistoryAction'
    )


def create_hist_sortie(num_serie: str, type_rame: str, voie: int, auteur: str) -> HistoryAction:
    return HistoryAction(
        num_serie=num_serie,
        type_rame=type_rame,
        operateur=auteur,
        action='sortie',
        voie=voie,
        _class='rtApi.model.HistoryAction'
    )
