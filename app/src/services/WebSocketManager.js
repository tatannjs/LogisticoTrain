// eslint-disable-next-line import/no-unresolved
import { ActivationState, Client } from '@stomp/stompjs';
import {
  computed, makeObservable, observable, runInAction,
} from 'mobx';
import SockJS from 'sockjs-client';

class WebsocketManager {
  _endpoint;

  // fonction asynchrone devant retournée un jeton CSRF utilisable
  // pour la connexion sous la forme d'un objet { headerName, token }
  _csrfTokenRetriever;

  _client = null;

  // Disctionnaire des souscription: liste de listener
  // et souscription par destination de souscriptions
  _subscriptions = new Map();

  _isConnecting = false;

  _connected = false;

  _onError = null;

  constructor(endpoint, csrfTokenRetriever) {
    makeObservable(this, {
      _isConnecting: observable,
      _connected: observable,
      _onError: observable,
      connecting: computed,
      connected: computed,
      state: computed,
      isActive: computed,
      onError: computed,
    });
    this._endpoint = endpoint;
    this._csrfTokenRetriever = csrfTokenRetriever;
  }

  get connecting() {
    return this._isConnecting;
  }

  get connected() {
    return this._connected;
  }

  get state() {
    return this._client?.state ?? null;
  }

  get isActive() {
    return this._client?.state === ActivationState.ACTIVE;
  }

  get onError() {
    return this._onError;
  }

  async connect(forceReconnection = false, extraClientOptions = {}) {
    // Si on est déjà en train de se connecter, on retourne simplement la promesse de connexion
    if (this.connecting) {
      return this;
    }

    // Si on est déjà connecté (on a un client)
    if (this.isActive) {
      // Si la reconnection forcée est demandée, on deconnecte,
      // sinon on retourne simplement le gestionnaire
      if (forceReconnection) {
        await this.disconnect();
      } else {
        return this;
      }
    }
    // Récupère le jeton CSRF, puis créer le client et le connecte
    const connectHeaders = {};
    if (this._csrfTokenRetriever) {
      const { headerName, token } = await this._csrfTokenRetriever();
      connectHeaders[headerName] = token;
    }
    // Préparation du client
    const client = new Client({
      ...extraClientOptions, // injection des options supplémentaire
      webSocketFactory: () => new SockJS(this._endpoint),
      connectHeaders,
      onConnect: () => {
        // On est connecté
        // remise à false de l'indicateur
        runInAction(() => {
          this._isConnecting = false;
          this._connected = true;
        });
        // On  souscrit à toutes les destinations déjà enregistrées
        this._subscribeToAll();
      },
      onDisconnect: (/* iMessage */) => {
        runInAction(() => {
          this._connected = false;
        });
      },
      onStompError: (iMessage) => {
        console.warn('STOMP ERROR', iMessage);
        runInAction(() => {
          this._onError = iMessage;
        });
        // the client will disconnect after this
      },
      onWebSocketClose: (/* evt */) => {
        runInAction(() => {
          this._connected = false;
        });
      },
      onWebSocketError: (evt) => {
        console.warn('WEBSOCKET ERROR', evt);
        runInAction(() => {
          this._onError = evt;
        });
      },
    });
    // Mise en place du client et connexion
    runInAction(() => {
      this._client = client;
      this._isConnecting = true;
      this._onError = null;
    });
    client.activate();
    return this;
  }

  async disconnect() {
    // Si on est actif, on desouscrit toutes les destinations connus puis on déconnecte
    if (this.isActive) {
      this._unsubscribeAll();
      try {
        await this._client.deactivate();
      } catch (e) {
        console.warn(`STOMP Client Discconect error: ${e.message}`);
      }
    }
    // Dans tous les cas, on efface le client et on reset  les indicateur
    runInAction(() => {
      this._client = null;
      this._isConnecting = false;
      this._connected = false;
    });
    return true; // Juste pour retourner une information de base à la promesse
  }

  /**
   * Envoie un message sur la socket, si celle-ci est connectée
   * @param {String} destination
   * @param {Any} message
   */
  publish(destination, message) {
    if (!this.isActive) {
      throw new Error('Socket not connected or inactive');
    }
    this._client.publish({ destination, body: JSON.stringify(message, null, 0) });
  }

  /**
   * Ajoute un écouteur sur une destination de souscription.
   * Souscrit à celle-ci si ça n'est pas déjà le cas.
   *
   * @param {String} destination la destination de souscription
   * @param {Function} listener la fonction à invoquer
   * @returns une fonction de retrait du listener
   */
  addListener(destination, listener) {
    // Si la destination n'existe pas déjà, l'ajoute et souscrit à celle ci si l'on est connecté
    if (!this._subscriptions.has(destination)) {
      const subInfo = {
        stompSuscription: null,
        listeners: new Set([listener]),
      };
      this._subscriptions.set(destination, subInfo);
      if (this.isActive) {
        subInfo.stompSuscription = this._subscribe(destination);
      }
    } else {
      // Ajoute le listener à la liste
      this._subscriptions.get(destination).listeners.add(listener);
    }
    // Retourne une fonction de retrait du listenr
    return () => this.removeListener(destination, listener);
  }

  /**
   * Retire un écouteur de sa destination de souscription.
   *
   * @param {String} destination la destination de souscription
   * @param {Function} listener la fonction à invoquer
   * @returns true si le listener a été retiré, false si ce dernier ou la destination
   * n'était pas présent
   */
  removeListener(destination, listener) {
    // ne fais rien si la destination n'est pas présente
    if (!this._subscriptions.has(destination)) {
      return false;
    }
    // Récupère l'objet complet de souscription
    const subInfo = this._subscriptions.get(destination);
    // Retire le listener. Retourne si ce dernier n'était pas présent
    if (!subInfo.listeners.delete(listener)) {
      return false;
    }
    // Si plus aucun listener, et une souscription courante desouscrit à la destination
    if (subInfo.listeners.size === 0) {
      if (this.isActive) {
        try {
          subInfo.stompSuscription.unsubscribe();
        } catch (err) {
          console.warn('Error while unsubscribine STOMP destination', err);
        } finally {
          subInfo.stompSuscription = null;
        }
      }
      // Retire la destination des souscriptions
      this._subscriptions.delete(destination);
    }
    return true;
  }

  _subscribeToAll() {
    this._subscriptions.forEach((subInfo, destination) => {
      try {
        // eslint-disable-next-line no-param-reassign
        subInfo.stompSuscription = this._subscribe(destination);
      } catch (e) {
        console.warn('Unable to subscript to STOMP destination', e);
      }
    });
  }

  _unsubscribeAll() {
    this._subscriptions.forEach((subInfo) => {
      try {
        subInfo.stompSuscription.unsubscribe();
      } catch (err) {
        console.warn('Error while unsubscribine STOMP destination', err);
      } finally {
        // eslint-disable-next-line no-param-reassign
        subInfo.stompSuscription = null;
      }
    });
  }

  _subscribe(destination) {
    return this._client.subscribe(destination, (iMessage) => {
      // Ne traite l'information reçue que si il y a des listeners
      if (this._subscriptions.has(destination)) {
        try {
          // Récupère les listener, parse les données du message
          const { listeners } = this._subscriptions.get(destination);
          const data = JSON.parse(iMessage.body);
          // Invoque chaque listener en passe les données et au cas ou le message d'origine
          // Chaque invocation est "protégée" pour éviter d'interrompre le traitement
          // en cas d'erreur sur un listener
          listeners.forEach((listener) => {
            try {
              listener(data, this, iMessage);
            } catch (listenerError) {
              console.warn('Error while calling listener', listenerError);
            }
          });
        } catch (msgError) {
          console.warn('Error while handling incoming message.', msgError);
        }
      } else {
        console.warn('Message received without any listener for it');
      }
    });
  }

  static buildWithDefaultOptions() {
    return new WebsocketManager(`${APP_ENV_RT_API_BASE_URL}/websocket`);
  }
}

export default WebsocketManager;
