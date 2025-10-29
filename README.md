# Plan de déploiement — Logistico-Train

## Étapes principales

1. **Préparation** : Configurer les secrets et volumes (cf. sections "Secrets" et "Volumes persistants")
2. **Déploiement** : Builder la WebApp puis démarrer les services principaux

# Description

##  Services Docker Compose


### 1. ProductionDB (Base de données de production)
**Service** : `sqldatabase`
- **Image** : MariaDB 11
- **Usage** : Stockage de l'état actuel du centre (rames, voies, tâches)
- **Volumes** :
  - `sqldata` : Stockage persistant des données (/var/lib/mysql)
  - Script d'initialisation : `init.sql` (création des tables au premier démarrage)
- **Réseaux** :
  - `sql-net` : Réseau isolé pour accès aux bases de données
- **Configuration** :
  - Variables d'environnement pour les credentials
  - Healthcheck : vérification toutes les 10 s
  - Restart policy : `unless-stopped`
  

---

### 2. HistoryDB (Base de données d'historique)
**Service** : `nosqldatabase`
- **Image** : MongoDB 7
- **Usage** : Stockage de l'historique de toutes les actions (demandes, entrées, sorties, tâches)
- **Volumes** :
  - `nosqldata` : Stockage persistant des documents (/data/db)
- **Réseaux** :
  - `nosql-net` : Réseau isolé pour accès aux bases de données (même réseau que sqldatabase)
- **Configuration** :
  - Variables d'environnement pour les credentials
  - Healthcheck : vérification toutes les 10 s
  - Restart policy : `unless-stopped`
- **Secrets** :
  - Credentials MongoDB (username/password)

---

### 3. MOM Broker (Message Broker temps réel)
**Service** : `broker`
- **Image** : `rabbitmq:3.12-management`
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
  - Healthcheck : vérification toutes les 10 s
  - Restart policy : `unless-stopped`
- **Secrets** :
  - Credentials RabbitMQ (username/password)

---

### 4. REST API (API de gestion)
**Service** : `restapi`
- **Image** : Python 3.11 (image personnalisée avec code embarqué)
- **Usage** : API REST pour consultation d'état, gestion des voies, inscription de tâches
- **Build** : Dockerfile avec code précompilé (base stable)
- **Volumes** : Aucun (code embarqué dans l'image pour optimisation)
- **Réseaux** :
  - `sql-net` : Accès aux bases de données
  - `nosql-net`
  - `front-net` : Communication avec front (reverse proxy)
- **Dépendances** :
  - sqldatabase (condition: service_healthy)
  - nosqldatabase (condition: service_healthy)
- **Configuration** :
  - Restart policy : `unless-stopped`
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
  - `nosql-net`
  - `broker-net` : Communication avec RabbitMQ
  - `front-net` : Communication avec front (reverse proxy)
- **Dépendances** :
  - sqldatabase (condition: service_healthy)
  - nosqldatabase (condition: service_healthy)
  - broker (condition: service_healthy)
- **Configuration** :
  - Command : Maven build + run (mvn spring-boot:run)
  - Restart policy : `unless-stopped`
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
  - Healthcheck : vérification toutes les 10 s
  - Restart policy : `unless-stopped`

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
  - `nosql-net` : Accès à nosqldatabase uniquement
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

##  Topologie réseau

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

##  Volumes persistants

### Volumes nommés (gérés par Docker)
- **`sqldata`** : Données MariaDB (/var/lib/mysql)
- **`nosqldata`** : Données MongoDB (/data/db)
- **`maven-cache`** : Cache Maven (~/.m2/repository) - performances
- **`maven-target`** : Compilation Java (./target) - persistance du build
- **`webapp-build`** : Build React - partagé entre webapp (écriture) et front (lecture seule)

### Bind mounts (montage depuis l'hôte)
- **./app** → webapp (code source React)
- **./RealtimeAPI** → wsapi (code source Java Spring)
- **./vendorConfigurations/nginx.conf** → front (configuration Nginx)
- **./init-db/init.sql** → sqldatabase (script d'initialisation BD)

---

##  Secrets (à compléter)

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

##  Commandes de déploiement

```powershell
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

---

## 🚀 Guide d'exécution complet

### Prérequis
```powershell
# Vérifier Docker
docker --version
docker-compose --version
```

### 1. Démarrer les bases de données
```powershell
# Base SQL (MariaDB)
docker-compose up -d sqldatabase

# Base NoSQL (MongoDB) 
docker-compose up -d nosqldatabase

# Vérifier le statut
docker-compose ps sqldatabase nosqldatabase
```

### 2. Démarrer le message broker
```powershell
# RabbitMQ avec management UI
docker-compose up -d broker

# Vérifier RabbitMQ
docker-compose ps broker
```

### 3. Démarrer les APIs
```powershell
# API REST Python
docker-compose up -d restapi

# API WebSocket Spring Boot
docker-compose up -d wsapi

# Vérifier les APIs
docker-compose ps restapi wsapi
```

### 4. Démarrer les outils d'administration
```powershell
# phpMyAdmin (port 8888)
docker-compose --profile dev-tool up -d phpmyadmin

# Mongo Express (port 8889)
docker-compose --profile dev-tool up -d mongo-express
```

### 5. Builder et démarrer le frontend
```powershell
# Builder l'application React
docker-compose --profile build run webapp

# Démarrer le serveur web Nginx
docker-compose up -d front
```

### 6. Commandes de test et vérification
```powershell
# Vérifier tous les conteneurs
docker-compose ps

# Tester la base SQL
docker exec logistico_sql_db mariadb -u root -plogistico_root_2024 -e "SHOW DATABASES;"

# Tester la base NoSQL
docker exec logistico_nosql_db mongosh --eval "db.runCommand({ping: 1})"

# Tester RabbitMQ Management API
curl -u rabbitmq_user:rabbitmq_pass_2024 http://localhost:15672/api/overview

# Voir les logs d'un service
docker-compose logs -f [nom_service]
```

### 7. Accès aux interfaces web
- **Application principale** : http://localhost
- **phpMyAdmin** : http://localhost:8888
- **Mongo Express** : http://localhost:8889  
- **RabbitMQ Management** : http://localhost:15672 (rabbitmq_user / rabbitmq_pass_2024)

### 8. Commandes de gestion
```powershell
# Arrêter tout
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v

# Redémarrer un service
docker-compose restart [nom_service]

# Reconstruire une image
docker-compose build [nom_service]

# Voir l'utilisation des ressources
docker stats
```

### 9. Identifiants configurés
- **MySQL root** : `logistico_root_2024`
- **MySQL user** : `logistico_user` / `logistico_pass_2024`
- **MongoDB** : `logistico_admin` / `mongo_pass_2024`
- **RabbitMQ** : `rabbitmq_user` / `rabbitmq_pass_2024`

---

## 🔧 Explications techniques et choix d'architecture

### Problèmes rencontrés et solutions

#### 1. Erreur 500 Frontend Nginx (Résolu)
**Problème** : L'application React retournait une erreur 500 avec le message "rewrite or internal redirection cycle while internally redirecting to /index.html"

**Cause racine** : 
- Le volume Docker était monté sur `/usr/share/nginx/html` (répertoire par défaut de Nginx)
- Mais la configuration `nginx.conf` définissait `root /var/www/app`
- Nginx ne trouvait pas les fichiers et créait une boucle de redirection

**Solution appliquée** :
1. Modification du `docker-compose.yaml` : Volume monté sur `/var/www/app` au lieu de `/usr/share/nginx/html`
2. Copie manuelle des fichiers React build dans le conteneur
3. Résultat : Frontend opérationnel sur http://localhost

#### 2. Configuration des volumes et build React
**Choix technique** : Utilisation d'un service `webapp` dédié pour builder React
- **Avantage** : Séparation des responsabilités (build vs serving)
- **Volume nommé** : `webapp-build` pour partager les fichiers entre builder et Nginx
- **Build process** : Webpack en mode production génère les assets optimisés

### Architecture réseau sécurisée

#### Isolation par réseaux Docker
**Choix** : 4 réseaux isolés pour sécuriser les communications

1. **`sql-net`** : Bases de données SQL/NoSQL + APIs + outils admin
2. **`nosql-net`** : MongoDB isolé (actuellement fusionné avec sql-net)
3. **`broker-net`** : RabbitMQ + WebSocket API
4. **`front-net`** : Frontend + APIs backend

**Avantages** :
- Isolation des couches (frontend, backend, données, messaging)
- Sécurité : Chaque service n'accède qu'aux ressources nécessaires
- Monitoring : Trafic réseau traceable par couche

#### Gestion des secrets
**Méthode** : Docker Secrets avec fichiers externes
- **Sécurité** : Credentials stockés dans `secrets/` (gitignore recommandé)
- **Flexibilité** : Changement des mots de passe sans rebuild des images
- **Best practice** : Variables d'environnement pointent vers les secrets

### Choix des technologies

#### Base de données polyglotte
- **MariaDB 11** : Données relationnelles, ACID, transactions
- **MongoDB 7** : Documents JSON, scalabilité horizontale, NoSQL

#### Message Broker
- **RabbitMQ 3.12** : Message queuing robuste, interface de management
- **Utilisation** : Communication asynchrone entre services

#### Frontend/Backend
- **React + Webpack** : SPA moderne, build optimisé pour production
- **Nginx Alpine** : Serveur web léger, haute performance
- **Spring Boot 3.3.3** : APIs REST/WebSocket, écosystème Java mature
- **Python Flask** : API REST légère, intégration rapide

### Points d'amélioration identifiés

1. **APIs en redémarrage** : REST Python et Spring Boot nécessitent des corrections
2. **Upgrade Spring Boot** : Migration vers 3.5.x planifiée
3. **Monitoring** : Ajout de health checks et métriques
4. **Tests** : Suite de tests end-to-end à implémenter
