import { makeAutoObservable, runInAction } from 'mobx';
import { ROOT_AX, ROOT_URL } from '../RESTInfo';
import HistoryAction from './HistoryAction';

class Rame {
  _numSerie;

  _typeRame;

  _numVoie = null;

  _conducteurEntrant;

  _taches = [];

  _actions = [];

  _loading = null;

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

  get numVoie() {
    return this._numVoie;
  }

  set numVoie(nv) {
    this._numVoie = nv;
  }

  get hasVoie() {
    return (this._numVoie ?? false) !== false;
  }

  get conducteurEntrant() {
    return this._conducteurEntrant;
  }

  get taches() {
    return this._taches;
  }

  get actions() {
    return this._actions;
  }

  fromJson(jsonData) {
    this._numSerie = jsonData?.numSerie ?? this._numSerie;
    this._typeRame = jsonData?.typeRame ?? this._typeRame;
    this._numVoie = jsonData?.voie ?? this._numVoie;
    this._conducteurEntrant = jsonData?.conducteurEntrant ?? this._conducteurEntrant;
    this._taches = jsonData?.taches ?? this._taches;
  }

  get isloading() {
    return this._loading === true;
  }

  get loadingError() {
    if (this._loading && this._loading !== true) {
      return this._loading;
    }
    return null;
  }

  async refresh({ withDetails = false, withHistory = false }) {
    const params = {};
    if (withDetails) {
      params.details = true;
    }
    try {
      runInAction(() => {
        this._loading = true;
      });
      const promises = [
        ROOT_AX.get(`${ROOT_URL}/rames/${this.numSerie}`, { params }).then((res) => res.data),
      ];
      if (withHistory) {
        promises.push(ROOT_AX.get(`${ROOT_URL}/actions`, {
          params: {
            limit: -1,
            numSerie: this.numSerie,
          },
        }).then((res) => res.data.map((rawAct) => new HistoryAction(rawAct))));
      }
      const allData = await Promise.all(promises);
      runInAction(() => {
        this.fromJson(allData[0]);
        if (withHistory) {
          // eslint-disable-next-line prefer-destructuring
          this._actions = allData[1];
        }
        this._loading = false;
      });
      return this;
    } catch (e) {
      runInAction(() => {
        this._loading = e;
      });
      return this;
    }
  }

  async validateTaches({ tacheIndexes, operateur }) {
    if (!tacheIndexes || !operateur || !tacheIndexes.length) {
      throw new Error('missing required information');
    }
    try {
      runInAction(() => {
        this._loading = true;
      });
      const data = await ROOT_AX.post(`${ROOT_URL}/rames/${this.numSerie}/actions`, {
        auteur: operateur,
        action: 'realTaches',
        taches: tacheIndexes,
      }).then((res) => res.data);
      runInAction(() => {
        this.fromJson(data);
        if (data.actions?.length) {
          this._actions.unshift(...data.actions);
        }
        this._loading = null;
      });
      return this;
    } catch (e) {
      runInAction(() => {
        this._loading = e;
      });
      return this;
    }
  }

  async addTaches({ taches, operateur }) {
    if (!taches || !operateur || !taches.length) {
      throw new Error('missing required information');
    }
    try {
      runInAction(() => {
        this._loading = true;
      });
      const data = await ROOT_AX.post(`${ROOT_URL}/rames/${this.numSerie}/actions`, {
        auteur: operateur,
        action: 'ajoutTaches',
        taches,
      }).then((res) => res.data);
      runInAction(() => {
        this.fromJson(data);
        if (data.actions?.length) {
          this._actions.unshift(...data.actions);
        }
        this._loading = null;
      });
      return this;
    } catch (e) {
      runInAction(() => {
        this._loading = e;
      });
      return this;
    }
  }

  async remove(auteur) {
    if (!auteur) {
      throw new Error('Missing auteur to remove rame');
    }
    // Build the remove request
    const removeRequest = {
      messageType: 'remove',
      numSerie: this._numSerie,
      voie: this._numVoie,
      auteur,
    };
    return ROOT_AX.put(`${APP_ENV_RT_API_BASE_URL}/rest/rames/remove-order`, removeRequest);
  }

  static async getRames({ onVoieOnly = false } = { onVoieOnly: false }) {
    const params = {};
    if (onVoieOnly) {
      params['on-voie'] = true;
    }
    const data = await ROOT_AX.get(`${ROOT_URL}/rames`, { params }).then((res) => res.data);
    return data.map((r) => new Rame(r));
  }

  static async getRemovableRames() {
    const data = await ROOT_AX.get(`${ROOT_URL}/rames`, {
      params: { removable: true },
    }).then((res) => res.data);
    return data.map((r) => new Rame(r));
  }

  static createFromRequest(rq) {
    const rame = new Rame();
    rame._numSerie = rq.numSerie;
    rame._typeRame = rq.typeRame;
    rame._conducteurEntrant = rq.conducteurEntrant;
    rame._taches = [...rq.taches];
    rame._numVoie = null;
    return rame;
  }
}

export default Rame;
