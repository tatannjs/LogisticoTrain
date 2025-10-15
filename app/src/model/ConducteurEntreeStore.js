import { makeAutoObservable, runInAction } from 'mobx';
import WebsocketManager from '../services/WebSocketManager';

class ConducteurEntreeStore {
  _numSerie = '';

  _typeRame = '';

  _taches = [];

  _conducteur = '';

  _wsManager;

  _waitingForAnswer = false;

  _wsQueueUnsubscriber = null;

  _error;

  _answer = null;

  constructor(user) {
    makeAutoObservable(this, {
      _wsManager: false,
      _wsQueueUnsubscriber: false,
      _conducteur: false,
    });
    this._conducteur = user.username;
  }

  async init() {
    this._wsManager = WebsocketManager.buildWithDefaultOptions();
    this._wsManager.connect();
    // subscribe to error queue
    this._wsManager.addListener('/user/exchange/amq.direct/errors', (error) => this._handleRTError(error));
  }

  async destroy() {
    if (this._wsManager.connected) {
      await this._wsManager.disconnect();
    }
  }

  get numSerie() {
    return this._numSerie;
  }

  set numSerie(ns) {
    this._numSerie = ns;
    this._answer = null;
  }

  get typeRame() {
    return this._typeRame;
  }

  set typeRame(tr) {
    this._typeRame = tr;
    this._answer = null;
  }

  get taches() {
    return this._taches;
  }

  get error() {
    return this._error;
  }

  addNewTache() {
    this.taches.push('');
    this._answer = null;
  }

  replaceTache(tache, idx) {
    if (idx >= 0 && idx < this.taches.length) {
      this.taches[idx] = tache;
    }
    this._answer = null;
  }

  removeTache(idx) {
    if (idx >= 0 && idx < this.taches.length) {
      this.taches.splice(idx, 1);
    }
    this._answer = null;
  }

  get rtConnecting() {
    return this._wsManager.connecting;
  }

  get rtConnected() {
    return this._wsManager.connected;
  }

  get rtError() {
    return this._wsManager.onError;
  }

  get waitingForAnswer() {
    return this._waitingForAnswer;
  }

  get answer() {
    return this._answer;
  }

  reset() {
    // unsubscribe if present
    if (this._wsQueueUnsubscriber) {
      this._wsQueueUnsubscriber();
      this._wsQueueUnsubscriber = null;
    }
    // Reset form info: only numSerie and typeRame
    this._numSerie = '';
    this._typeRame = '';
    this._waitingForAnswer = false;
    this._answer = null;
  }

  async sendRequest() {
    if (this._waitingForAnswer) {
      throw new Error('Cannot send request that has been already sent');
    }
    // Remove all empty/null tash, and trim all field
    runInAction(() => {
      this._numSerie = this._numSerie.trim();
      this._typeRame = this._typeRame.trim();
      this._taches = this._taches.filter((t) => !!t).map((t) => t.trim());
    });
    // Check every field is ok
    if (!this._numSerie || !this._typeRame || !this._taches.length) {
      return;
    }
    runInAction(() => {
      this._waitingForAnswer = true;
      this._error = null;
    });
    // Add listiner for answer
    this._wsQueueUnsubscriber = this._wsManager.addListener(
      `/topic/rameaccess.${this._numSerie}`,
      (answer) => this._handleAnswer(answer),
    );

    // Send request
    this._wsManager.publish('/app/rameaccess', {
      messageType: 'entranceRequest',
      numSerie: this._numSerie,
      typeRame: this._typeRame,
      auteur: this._conducteur,
      taches: this._taches,
    });
  }

  async _handleAnswer(answer) {
    if (!answer.numSerie) {
      return;
    }
    // Unsubscrbie
    if (this._wsQueueUnsubscriber) {
      this._wsQueueUnsubscriber();
      this._wsQueueUnsubscriber = null;
    }
    runInAction(() => {
      this._waitingForAnswer = false;
      this._answer = answer;
      if (answer.accept) {
        // reset form if accept
        this._numSerie = '';
        this._typeRame = '';
        this._taches = [];
      }
    });
  }

  _handleRTError(error) {
    console.warn('Error', error);
    this._error = error;
    // If we have published a request, reset it
    if (this._waitingForAnswer) {
      if (this._wsQueueUnsubscriber) {
        this._wsQueueUnsubscriber();
        this._wsQueueUnsubscriber = null;
      }
      this._waitingForAnswer = false;
    }
  }
}

export default ConducteurEntreeStore;
