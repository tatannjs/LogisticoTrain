import { makeAutoObservable } from 'mobx';
import { ROOT_AX, ROOT_URL } from '../RESTInfo';
import Rame from './Rame';

class Voie {
  _numVoie;

  _interdite = false;

  _rame;

  constructor(jsonData) {
    makeAutoObservable(this);
    if (jsonData) {
      this.fromJson(jsonData);
    }
  }

  get numVoie() {
    return this._numVoie;
  }

  get interdite() {
    return this._interdite;
  }

  get rame() {
    return this._rame;
  }

  set rame(r) {
    this._rame = r;
  }

  fromJson(json) {
    this._numVoie = json?.numVoie ?? this._numVoie;
    this._interdite = json?.interdite ?? this._interdite;
    if (json?.rame) {
      this._rame = new Rame(json.rame);
    }
  }

  async delete() {
    await ROOT_AX.delete(`${ROOT_URL}/voies/${this._numVoie}`);
    return true;
  }

  async switchInterdite() {
    const data = await ROOT_AX.put(`${ROOT_URL}/voies/${this._numVoie}/interdite`, {
      interdite: !this._interdite,
    }).then((res) => res.data);
    this.fromJson(data);
    return this;
  }

  static async getVoies() {
    const data = await ROOT_AX.get(`${ROOT_URL}/voies`).then((res) => res.data);
    return data.map((v) => new Voie(v));
  }

  static async create({ numVoie, interdite }) {
    const res = await ROOT_AX.post(`${ROOT_URL}/voies`, {
      numVoie,
      interdite,
    });
    return new Voie(res.data);
  }
}

export default Voie;
