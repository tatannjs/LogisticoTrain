class LocalStorageManager {
  _simulated;

  _storage;

  constructor() {
    this._init();
  }

  _init() {
    try {
      this._storage = window.localStorage;
      const testedKey = '__STORAGE_TEST__';
      this._storage.setItem(testedKey, testedKey);
      this._storage.removeItem(testedKey);
      this._simulated = false;
    } catch (e) {
      if (e instanceof DOMException
        && (e.name === 'QuotaExceededError' || e.name === 'NS_ERROR_DOM_QUOTA_REACHED')
        && this._storage && this._storage.length !== 0) {
        console.warn('Max storage reached. Will simulate storage', e);
      } else {
        console.warn('Unavailable storage. Will simulate storage', e);
      }
      this._storage = new Map();
      this._simulated = true;
    }
  }

  async setItem(key, value) {
    if (!key || !value) {
      throw new Error('Missing key or value for storage');
    }
    if (this._simulated) {
      this._storage[key] = value;
    } else {
      this._storage.setItem(key, JSON.stringify(value, null, 0));
    }
    return value;
  }

  async getItem(key) {
    if (!key) {
      throw new Error('Missing key for storage');
    }
    if (this._simulated) {
      return this._storage[key];
    }
    const data = this._storage.getItem(key);
    if (data) {
      return JSON.parse(data);
    }
    return null;
  }

  async deleteItem(key) {
    if (!key) {
      throw new Error('Missing key for storage');
    }
    if (this._simulated) {
      this._storage.delete(key);
    } else {
      this._storage.removeItem(key);
    }
  }

  clear() {
    this._storage.clear();
  }

  * generateKeys() {
    if (this._simulated) {
      yield* Object.keys(this._storage);
    } else {
      for (let i = 0; i < this._storage.length; i += 1) {
        yield this._storage.key(i);
      }
    }
  }
}

const INSTANCE = new LocalStorageManager();

export default INSTANCE;
