import User from './model/User';

class GeneralStore {
  _main = null;

  _user = new User();

  get main() {
    return this._main;
  }

  get user() {
    return this._user;
  }

  async switchMain(Class_) {
    if (this._main?.destroy) {
      await this._main.destroy();
    }
    this._main = null;
    if (Class_) {
      this._main = new Class_(this._user);
      if (this._main.init) {
        await this._main.init();
      }
    }
    return this._main;
  }

  async resetMain() {
    if (this._main?.destroy) {
      await this._main.destroy();
    }
    this._main = null;
    return this._main;
  }
}

const STORE = new GeneralStore();

export default STORE;
