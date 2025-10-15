import { makeAutoObservable } from 'mobx';

class RameEntranceRequest {
  _numSerie;

  _typeRame;

  _auteur;

  _taches;

  constructor(jsonData) {
    makeAutoObservable(this);
    if (jsonData) {
      this.fromJson(jsonData);
    }
  }

  get numSerie() {
    return this._numSerie;
  }

  get typeRame() {
    return this._typeRame;
  }

  get auteur() {
    return this._auteur;
  }

  get taches() {
    return this._taches;
  }

  fromJson(jsonData) {
    this._numSerie = jsonData?.numSerie ?? this._numSerie;
    this._typeRame = jsonData?.typeRame ?? this._typeRame;
    this._auteur = jsonData?.auteur ?? this._auteur;
    this._taches = jsonData?.taches ?? this._taches;
  }

  static createFromRame(rame) {
    const request = new RameEntranceRequest();
    request._numSerie = rame.numSerie;
    request._typeRame = rame.typeRame;
    request._auteur = rame.conducteurEntrant;
    request._taches = [...rame.taches];
    return request;
  }
}

export default RameEntranceRequest;
