from sqlalchemy import String, Integer, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column

from dao.production_db import Base

__all__ = ['Rame']


class Rame(Base):
    __tablename__ = 'rames'

    num_serie: Mapped[str] = mapped_column(String(12), primary_key=True)
    type_rame: Mapped[str] = mapped_column(String(50), nullable=False)
    voie: Mapped[int] = mapped_column(ForeignKey("voies.num_voie"), unique=True, nullable=True)
    conducteur_entrant: Mapped[str] = mapped_column(String(50), nullable=False)

    def __init__(self, num_serie: str, type_rame: str, voie: int):
        self.num_serie = num_serie
        self.type_rame = type_rame
        self.voie = voie

    def __repr__(self) -> str:
        return f"Rame(numSerie={self.num_serie!r}, typeRame={self.type_rame!r}, voie={self.voie!r})"
