from sqlalchemy import String, Integer, ForeignKey, Boolean
from sqlalchemy.orm import Mapped, mapped_column

from dao.production_db import Base

__all__ = ['Voie']


class Voie(Base):
    __tablename__ = 'voies'

    num_voie: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=False)
    interdite: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)

    def __init__(self, num_voie: int, interdite: bool):
        self.num_voie = num_voie
        self.interdite = interdite

    def __repr__(self) -> str:
        return f"Voie(numVoie={self.num_voie!r}, interdite={self.interdite!r})"
