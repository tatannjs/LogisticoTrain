import LSM from '../services/LocalStorageManager';

const LSM_KEY = 'USER.currentUser';

class User {
  static roles = Object.freeze([
    { name: 'operateur', description: 'Opérateur centre' },
    { name: 'technicien', description: 'Technicien centre' },
    { name: 'conducteur', description: 'Conducteur rame' },
  ]);

  _username = null;

  _role = null;

  _ready;

  constructor() {
    this._ready = new Promise((resolve) => {
      this._initFromStorage().finally(() => resolve(true));
    });
  }

  get username() {
    return this._username;
  }

  get role() {
    return this._role;
  }

  get ready() {
    return this._ready;
  }

  authenticate(username, role) {
    if (!username || !role) {
      throw new Error('missing username or role');
    }
    if (!User.roles.some((r) => r.name === role)) {
      throw new Error(`Unknown role ${role}`);
    }
    this._username = username;
    this._role = role;
    this._saveToStorage();
  }

  desauthenticate() {
    this._username = null;
    this._role = null;
    this._saveToStorage();
  }

  get authenticated() {
    return !!this._username;
  }

  get isOperateur() {
    return this._role === 'operateur';
  }

  get isTechnicien() {
    return this._role === 'technicien';
  }

  get isConducteur() {
    return this._role === 'conducteur';
  }

  async _initFromStorage() {
    const data = await LSM.getItem(LSM_KEY);
    if (data?.username && data?.role && User.roles.some((r) => r.name === data.role)) {
      this._username = data.username;
      this._role = data.role;
    }
  }

  async _saveToStorage() {
    await LSM.setItem(LSM_KEY, {
      username: this._username,
      role: this._role,
    });
  }
}

export default User;
