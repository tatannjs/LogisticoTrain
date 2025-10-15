import { makeAutoObservable, runInAction } from 'mobx';
import WebsocketManager from '../services/WebSocketManager';
import Rame from './Rame';
import Voie from './Voie';
import RameEntranceRequest from './RameEntranceRequest';

class OperateurStore {
  _operateur = null;

  _loadingData = false;

  _wsManager;

  _voies = [];

  _rameEntranceRequests = [];

  _selectedRame = null;

  _error;

  constructor(user) {
    makeAutoObservable(this, {
      _operateur: false,
    });
    this._operateur = user.username;
  }

  async init() {
    runInAction(() => {
      this._loadingData = true;
    });
    this._wsManager = WebsocketManager.buildWithDefaultOptions();
    this._wsManager.connect();
    // subscribe to error queue
    this._wsManager.addListener('/user/exchange/amq.direct/errors', (error) => this._handleRTError(error));
    // subscribe to rame-access topic
    this._wsQueueUnsubscriber = this._wsManager.addListener(
      '/topic/rameaccess',
      (message) => this._handleRTMessage(message),
    );
    // Request voies and current rames
    const [voies, rames] = await Promise.all([
      Voie.getVoies(), Rame.getRames(),
    ]);
    // map rame in voies and for each rame without any voie, convert them into request
    const voiesByNumvoie = voies.reduce((acc, cur) => {
      acc[cur.numVoie] = cur;
      return acc;
    }, {});
    const pendingRequests = [];
    rames.forEach((rame) => {
      if (rame.hasVoie) {
        // set rame to voie
        const voie = voiesByNumvoie[rame.numVoie];
        if (!voie) {
          console.warn(`Should add a rame to an unknown voie ${rame.numVoie}`);
        } else {
          voie.rame = rame;
        }
      } else {
        // convert rame into current request
        const request = RameEntranceRequest.createFromRame(rame);
        pendingRequests.push(request);
      }
    });

    runInAction(() => {
      this._voies = voies;
      pendingRequests.forEach((r) => this._handleNewRequest(r));
      this._loadingData = false;
    });
  }

  async destroy() {
    if (this._wsManager.connected) {
      await this._wsManager.disconnect();
    }
  }

  get loadingData() {
    return this._loadingData;
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

  get voies() {
    return this._voies;
  }

  get voiesDisponibles() {
    return this._voies.filter((v) => !v.interdite && !v.rame);
  }

  get numVoies() {
    return this._voies.map((v) => v.numVoie);
  }

  get numVoiesDisponibles() {
    return this.voiesDisponibles.map((v) => v.numVoie);
  }

  get rameEntranceRequests() {
    return this._rameEntranceRequests;
  }

  get selectedRame() {
    return this._selectedRame;
  }

  get error() {
    return this._error;
  }

  selectRame(rame) {
    if (rame) {
      rame.refresh({ withDetails: true, withHistory: true });
    }
    this._selectedRame = rame;
  }

  async deleteVoie(voie) {
    if (voie.rame) {
      throw new Error('Cannot delete voie with a rame');
    }
    await voie.delete();
    runInAction(() => {
      this._voies = this._voies.filter((v) => v.numVoie !== voie.numVoie);
    });
  }

  // eslint-disable-next-line class-methods-use-this
  async switchVoieInterdite(voie) {
    if (voie.rame) {
      throw new Error('Cannot delete voie with a rame');
    }
    return voie.switchInterdite();
  }

  async rejectRameEntranceRequest(request) {
    this._wsManager.publish(`/app/rameaccess.${request.numSerie}`, {
      messageType: 'entranceAnswer',
      numSerie: request.numSerie,
      accept: false,
      auteur: this._operateur,
    });
    runInAction(() => {
      this._rameEntranceRequests = this._rameEntranceRequests
        .filter((r) => r.numSerie !== request.numSerie);
    });
  }

  async acceptRameEntranceRequest(request, numVoie) {
    const voie = this._voies.find((v) => v.numVoie === numVoie);
    if (!voie) {
      throw new Error('numVoie not related to any voie');
    }
    this._wsManager.publish(`/app/rameaccess.${request.numSerie}`, {
      messageType: 'entranceAnswer',
      numSerie: request.numSerie,
      accept: true,
      voie: numVoie,
      auteur: this._operateur,
    });
    // Convert request to rame and add it to voie
    const rame = Rame.createFromRequest(request);
    rame.numVoie = numVoie;
    voie.rame = rame;
    // Remove request from pending request
    runInAction(() => {
      const requestIdx = this._rameEntranceRequests.findIndex((r) => r === request);
      if (requestIdx >= 0) {
        this._rameEntranceRequests.splice(requestIdx, 1);
      }
    });
  }

  async createNewVoie(numVoie) {
    if ((numVoie ?? false) === false) {
      throw new Error('numVoie must be given to create a new voie');
    }
    const voie = await Voie.create({ numVoie, interdite: false });
    this._voies.push(voie);
    return voie;
  }

  _handleNewRequest(request) {
    if (this._rameEntranceRequests.some((r) => r.numSerie === request.numSerie)) {
      console.warn('duplicated request. drop.');
      return;
    }
    this._rameEntranceRequests.push(request);
  }

  _handleRameRemove(removeMsg) {
    // find the voie
    const voie = this._voies.find((v) => v.numVoie === removeMsg.voie);
    if (!voie) {
      console.warn(`receive remove message with unknown voie: ${removeMsg.voie}`);
      return;
    }
    // Check the rame on the voie has the proper numSerie
    if (!voie.rame || voie.rame.numSerie !== removeMsg.numSerie) {
      console.warn(`receive remove message with unproper numSerie: ${removeMsg.numSerie}`);
      return;
    }
    // Remove the rame
    voie.rame = null;
  }

  _handleRTMessage(message) {
    switch (message?.messageType) {
      case 'entranceRequest':
        this._handleNewRequest(new RameEntranceRequest(message));
        break;
      case 'remove':
        this._handleRameRemove(message);
        break;
      default:
        console.warn(`Receive unknown message type: ${message?.messageType}`);
    }
  }

  _handleRTError(error) {
    console.warn('Error', error);
    this._error = error;
  }
}

export default OperateurStore;
