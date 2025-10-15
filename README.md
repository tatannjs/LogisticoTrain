# Plan de déploiement Logistico-Train

## Etape 1

## Etape 2

# Description

## 📋 Services Docker Compose


### 1. ProductionDB (Base de données de production)
**Service** : `sqldatabase`
- **Image** : MariaDB 11
- **Usage** : Stockage de l'état actuel du centre (rames, voies, tâches)
- **Volumes** :
  - `sqldata` : Stockage persistant des données (/var/lib/mysql)
  - Script d'initialisation : init.sql (création des tables au premier démarrage)
- **Réseaux** :
  - `sql-net` : Réseau isolé pour accès aux bases de données
- **Configuration** :
  - Variables d'environnement pour credentials
  - Healthcheck : vérification toutes les 10s
  - Restart policy : unless-stopped
  

---

### 2. HistoryDB (Base de données d'historique)
**Service** : `nosqldatabase`
- **Image** : MongoDB 7
- **Usage** : Stockage de l'historique de toutes les actions (demandes, entrées, sorties, tâches)
- **Volumes** :
  - `nosql` : Stockage persistant des documents (/data/db)
- **Réseaux** :
  - `sql-net` : Réseau isolé pour accès aux bases de données (même réseau que sqldatabase)
- **Configuration** :
  - Variables d'environnement pour credentials
  - Healthcheck : vérification toutes les 10s
  - Restart policy : unless-stopped
- **Secrets** :
  - Credentials MongoDB (username/password)

---

### 3. MOM Broker (Message Broker temps réel)
**Service** : `broker`
- **Image** : RabbitMQ 3.12-management
- **Usage** : Gestion des notifications temps réel entre conducteurs et opérateurs
- **Volumes** : Aucun (messages éphémères, données déjà persistées dans SQL + NoSQL)
  - *Note* : Un volume optionnel peut être ajouté pour persister les queues (/var/lib/rabbitmq)
- **Réseaux** :
  - `broker-net` : Réseau isolé pour communication avec wsapi
- **Ports** :
  - 5672 : AMQP (communication serveur)
  - 15672 : Interface de management web
  - 61613 : STOMP (WebSocket pour webapp)
- **Configuration** :
  - Plugin STOMP activé
  - Healthcheck : vérification toutes les 10s
  - Restart policy : unless-stopped
- **Secrets** :
  - Credentials RabbitMQ (username/password)

---

### 4. REST API (API de gestion)
**Service** : `restapi`
- **Image** : Python 3.11 (image personnalisée avec code embarqué)
- **Usage** : API REST pour consultation état, gestion voies, inscription tâches
- **Build** : Dockerfile avec code précompilé (base stable)
- **Volumes** : Aucun (code embarqué dans l'image pour optimisation)
- **Réseaux** :
  - `sql-net` : Accès aux bases de données
  - `front-net` : Communication avec front (reverse proxy)
- **Dépendances** :
  - sqldatabase (condition: service_healthy)
  - nosqldatabase (condition: service_healthy)
- **Configuration** :
  - Restart policy : unless-stopped
- **Secrets** :
  - Credentials bases de données (SQL + NoSQL)
  - Fichier de configuration complet si nécessaire

---

### 5. RealTime API (API temps réel)
**Service** : `wsapi`
- **Image** : Eclipse Temurin JDK 21
- **Usage** : WebSocket pour demandes/acceptations/sorties de rames + notifications temps réel
- **Volumes** :
  - Code source monté : ./RealtimeAPI (bind mount, modifications fréquentes)
  - `maven-cache` : Cache Maven persistant (~/.m2/repository) - performances
  - `maven-target` : Dossier de compilation (./target) - persistance du build
- **Réseaux** :
  - `sql-net` : Accès aux bases de données
  - `broker-net` : Communication avec RabbitMQ
  - `front-net` : Communication avec front (reverse proxy)
- **Dépendances** :
  - sqldatabase (condition: service_healthy)
  - nosqldatabase (condition: service_healthy)
  - broker (condition: service_healthy)
- **Configuration** :
  - Command : Maven build + run (mvn spring-boot:run)
  - Restart policy : unless-stopped
- **Secrets** :
  - Credentials bases de données (SQL + NoSQL)
  - Credentials RabbitMQ

---

### 6. HTTP Server (Serveur frontend)
**Service** : `front`
- **Image** : Nginx Alpine (distribution minimale de sécurité)
- **Usage** : Point d'entrée des clients, sert fichiers statiques + reverse proxy vers APIs
- **Volumes** :
  - `webapp-build` : Build de l'application React (lecture seule)
  - Configuration Nginx personnalisée : ./vendorConfigurations/nginx.conf (lecture seule)
- **Réseaux** :
  - `front-net` : Reverse proxy vers restapi + wsapi
- **Ports** :
  - 80:80 : HTTP
- **Dépendances** :
  - restapi
  - wsapi
  - webapp (build doit être terminé)
- **Configuration** :
  - Healthcheck : vérification toutes les 10s
  - Restart policy : unless-stopped

---

### 7. WebApp Builder (Construction application React)
**Service** : `webapp`
- **Image** : Node.js 22
- **Usage** : Build de l'application web cliente (React + Webpack)
- **Volumes** :
  - Code source monté : ./app (bind mount, lecture seule)
  - `webapp-build` : Sortie du build (écriture) - partagé avec front
- **Réseaux** :
  - Bridge par défaut (pas besoin de réseau custom, pas de communication avec autres services)
- **Profiles** : `build` (ne se lance pas avec docker compose up par défaut)
- **Configuration** :
  - Command : npm install && npm run build
  - Fichier de configuration : webpack.prod.js

---

### 8. phpMyAdmin (Outil de développement)
**Service** : `phpmyadmin`
- **Image** : phpMyAdmin latest
- **Usage** : Interface web pour administrer MariaDB
- **Volumes** : Aucun
- **Réseaux** :
  - `sql-net` : Accès à sqldatabase uniquement
- **Ports** :
  - 127.0.0.1:8888:80 : Interface web (localhost uniquement, sécurisé)
- **Dépendances** :
  - sqldatabase
- **Profiles** : `dev-tool` (ne se lance qu'avec --profile dev-tool)
- **Configuration** :
  - Restart policy : always (outil de développement disponible en permanence)
- **Secrets** :
  - Utilise les credentials de sqldatabase 

---

### 9. Mongo Express (Outil de développement)
**Service** : `mongo-express`
- **Image** : Mongo Express latest
- **Usage** : Interface web pour administrer MongoDB
- **Volumes** : Aucun
- **Réseaux** :
  - `sql-net` : Accès à nosqldatabase uniquement
- **Ports** :
  - 127.0.0.1:8889:8081 : Interface web (localhost uniquement, sécurisé)
- **Dépendances** :
  - nosqldatabase
- **Profiles** : `dev-tool` (ne se lance qu'avec --profile dev-tool)
- **Configuration** :
  - Restart policy : always (outil de développement disponible en permanence)
- **Secrets** :
  - Utilise les credentials de nosqldatabase

---

## 🌐 Topologie réseau

### Réseaux définis

- **`sql-net`** : Réseau isolé pour bases de données
  - Membres : sqldatabase, nosqldatabase, restapi, wsapi, phpmyadmin, mongo-express

- **`broker-net`** : Réseau isolé pour message broker
  - Membres : broker, wsapi

- **`front-net`** : Réseau frontend-backend
  - Membres : restapi, wsapi, front

- **Bridge par défaut** : Pour webapp (pas de communication avec autres services)

### Isolation et sécurité
- Les bases de données ne sont accessibles que par les APIs et outils dev
- Le broker est isolé et accessible uniquement par wsapi
- Front communique uniquement avec les APIs (pas d'accès direct aux BD)
- Outils de développement isolés sur localhost uniquement

---

## 💾 Volumes persistants

### Volumes nommés (gérés par Docker)
- **`sqldata`** : Données MariaDB (/var/lib/mysql)
- **`nosql`** : Données MongoDB (/data/db)
- **`maven-cache`** : Cache Maven (~/.m2/repository) - performances
- **`maven-target`** : Compilation Java (./target) - persistance du build
- **`webapp-build`** : Build React - partagé entre webapp (écriture) et front (lecture seule)

### Bind mounts (montage depuis l'hôte)
- **./app** → webapp (code source React)
- **./RealtimeAPI** → wsapi (code source Java Spring)
- **./vendorConfigurations/nginx.conf** → front (configuration Nginx)
- **./init-db/init.sql** → sqldatabase (script d'initialisation BD)

---

## 🔐 Secrets (à compléter)

### Secrets pour bases de données
- **`mysql_root_password`** : Mot de passe root MariaDB
- **`mysql_user`** : Utilisateur applicatif MariaDB
- **`mysql_password`** : Mot de passe utilisateur MariaDB
- **`mongo_root_username`** : Utilisateur admin MongoDB
- **`mongo_root_password`** : Mot de passe admin MongoDB

### Secrets pour broker
- **`rabbitmq_user`** : Utilisateur RabbitMQ
- **`rabbitmq_password`** : Mot de passe RabbitMQ

### Configurations complètes (si nécessaire)
- **`restapi_config`** : Configuration complète de l'API REST (si credentials non externalisables)
- **`wsapi_application_properties`** : application.properties de wsapi (si credentials non externalisables)

### Notes de sécurité
- Aucun mot de passe en clair dans docker-compose.yml
- Secrets stockés dans des fichiers externes ou Docker secrets
- Accès aux secrets en lecture seule pour les services

---

## 🚀 Commandes de déploiement

```bash
# Lancer les services principaux
docker compose up -d

# Lancer avec outils de développement
docker compose --profile dev-tool up -d

# Builder l'application web
docker compose --profile build run webapp

# Arrêter tous les services
docker compose down

# Voir les logs
docker compose logs -f [service]
```
