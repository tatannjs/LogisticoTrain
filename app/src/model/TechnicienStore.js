import { makeAutoObservable, runInAction } from 'mobx';
import Rame from './Rame';
import Voie from './Voie';

class TechnicienStore {
  _operateur;

  _loadingData = false;

  _voies = [];

  _selectedRame = null;

  _selectedTasks = null;

  _creatingTasks = false;

  _error;

  constructor(user) {
    makeAutoObservable(this, {
      _operateur: false,
    });
    this._operateur = user.username;
  }

  async init() {
    // Request voies and current rames
    const [voies, rames] = await Promise.all([
      Voie.getVoies(), Rame.getRames({ onVoieOnly: true }),
    ]);
    // map rame in voies and for each rame without any voie, convert them into request
    const voiesByNumvoie = voies.reduce((acc, cur) => {
      acc[cur.numVoie] = cur;
      return acc;
    }, {});
    rames.forEach((rame) => {
      const voie = voiesByNumvoie[rame.numVoie];
      if (!voie) {
        console.warn(`Should add a rame to an unknown voie ${rame.numVoie}`);
      } else {
        voie.rame = rame;
      }
    });
    runInAction(() => {
      this._voies = voies;
      this._loadingData = false;
    });
  }

  // eslint-disable-next-line class-methods-use-this
  async destroy() {
    // do nothing
  }

  get loadingData() {
    return this._loadingData;
  }

  get voies() {
    return this._voies;
  }

  get selectedRame() {
    return this._selectedRame;
  }

  get selectedTasks() {
    return this._selectedTasks;
  }

  get creatingTasks() {
    return this._creatingTasks;
  }

  get error() {
    return this._error;
  }

  selectRame(rame) {
    if (rame) {
      rame.refresh({ withDetails: true, withHistory: true });
    }
    this._selectedRame = rame;
    this._selectedTasks = [];
  }

  selectTask(task) {
    if (!task) {
      throw new Error('Missing task to select.');
    }
    if (!this._selectedRame) {
      throw new Error('Cannot select task without any selected rame.');
    }
    if (this._selectedTasks.some((t) => t === task)) {
      return;
    }
    this._selectedTasks.push(task);
  }

  unselectTask(task) {
    if (!task) {
      throw new Error('Missing task to unselect.');
    }
    if (!this._selectedRame) {
      throw new Error('Cannot select task without any selected rame');
    }
    const taskIdx = this._selectedTasks.findIndex((t) => t === task);
    if (taskIdx >= 0) {
      this._selectedTasks.splice(taskIdx, 1);
    }
  }

  switchTaskSelection(task) {
    if (!task) {
      throw new Error('Missing task to unselect.');
    }
    if (!this._selectedRame) {
      throw new Error('Cannot select task without any selected rame');
    }
    const taskIdx = this._selectedTasks.findIndex((t) => t === task);
    if (taskIdx >= 0) {
      this._selectedTasks.splice(taskIdx, 1);
    } else {
      this._selectedTasks.push(task);
    }
  }

  async createTasks(tasks) {
    if (!tasks?.length) {
      throw new Error('Missing task to add.');
    }
    if (!this._selectedRame) {
      throw new Error('Cannot create tasks without any selected rame');
    }
    runInAction(() => {
      this._creatingTasks = true;
    });
    // TODO: create tasks
    try {
      await this._selectedRame.addTaches({ taches: tasks, operateur: this._operateur });
      return true;
    } finally {
      runInAction(() => {
        this._creatingTasks = false;
      });
    }
  }

  async realizeTasks() {
    if (!this._selectedRame) {
      throw new Error('Cannot realize tasks without any selected rame');
    }
    if (!this._selectedTasks?.length) {
      throw new Error('Cannot realize tasks without any selected tasks');
    }
    const taskIdxs = this._selectedTasks.map((t) => t.idx);
    await this._selectedRame.validateTaches({ tacheIndexes: taskIdxs, operateur: this._operateur });
    // empty selected tasks
    runInAction(() => {
      this._selectedTasks = [];
    });
    return true;
  }
}

export default TechnicienStore;
