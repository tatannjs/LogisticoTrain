import { makeAutoObservable } from 'mobx';

class HistoryAction {
  _id;

  _numSerie;

  _typeRame;

  _action;

  _auteur;

  _timestamp;

  _voie;

  _taches;

  constructor(jsonData) {
    makeAutoObservable(this);
    if (jsonData) {
      this.fromJson(jsonData);
    }
  }

  get id() {
    return this._id;
  }

  get numSerie() {
    return this._numSerie;
  }

  get typeRame() {
    return this._typeRame;
  }

  get action() {
    return this._action;
  }

  get auteur() {
    return this._auteur;
  }

  get timestamp() {
    return this._timestamp;
  }

  get voie() {
    return this._voie;
  }

  get taches() {
    return this._taches;
  }

  fromJson(jsonData) {
    this._id = jsonData?.id ?? this._id;
    this._numSerie = jsonData?.numSerie ?? this._numSerie;
    this._typeRame = jsonData?.typeRame ?? this._typeRame;
    this._action = jsonData?.action ?? this._action;
    this._auteur = jsonData?.auteur ?? this._auteur;
    if (jsonData?.timestamp) {
      this._timestamp = new Date(jsonData.timestamp);
    }
    this._voie = jsonData?.voie ?? this._voie;
    this._taches = jsonData?.taches ?? this._taches;
  }
}

export default HistoryAction;
