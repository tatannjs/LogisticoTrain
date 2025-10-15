import { makeAutoObservable, runInAction } from 'mobx';
import Rame from './Rame';

class ConducteurSortieStore {
  _conducteur;

  _removableRames = [];

  _removing = false;

  _error = null;

  _rameRemoved = null;

  constructor(user) {
    makeAutoObservable(this, {
      _conducteur: false,
    });
    this._conducteur = user.username;
  }

  async init() {
    // Load rame that can be removed
    const rames = await Rame.getRemovableRames();
    runInAction(() => {
      this._removableRames = rames;
    });
  }

  // eslint-disable-next-line class-methods-use-this
  async destroy() {
    // do nothing
  }

  get removableRames() {
    return this._removableRames;
  }

  get removing() {
    return this._removing;
  }

  get error() {
    return this._error;
  }

  get rameRemoved() {
    return this._rameRemoved;
  }

  async removeRame(rame) {
    if (!rame) {
      throw new Error('A rame must be given to be removed.');
    }
    if (!this._removableRames.some((r) => r === rame)) {
      throw new Error('Unknown rame');
    }
    // clean state
    runInAction(() => {
      this._removing = true;
      this._error = null;
    });
    // Remove the rame
    try {
      // Order to remove
      await rame.remove(this._conducteur);
      // Remove from Array and set rame remove
      runInAction(() => {
        const rameIdx = this._removableRames.findIndex((r) => r === rame);
        if (rameIdx >= 0) {
          this._removableRames.splice(rameIdx, 1);
        }
        this._rameRemoved = rame;
        this._removing = false;
      });
    } catch (error) {
      runInAction(() => {
        this._error = error;
        this._removing = false;
      });
    }
  }
}

export default ConducteurSortieStore;
