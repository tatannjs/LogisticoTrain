#  Logistico-Train 
##  Vue d'ensemble

Application de gestion d'un centre de maintenance ferroviaire avec architecture microservices Docker.

**Technologies** : React (Frontend) + Python Flask (REST) + Spring Boot (WebSocket) + MariaDB + MongoDB + RabbitMQ + Nginx

**Architecture** : 9 services Docker orchestrés avec Docker Compose, 4 réseaux isolés, gestion complète des secrets

---

##  Statut du Projet

###  Services Opérationnels (6/7)
- **sqldatabase** : MariaDB 11  (Base de production)
- **nosqldatabase** : MongoDB 7  (Historique des actions)  
- **broker** : RabbitMQ 3.12  (Messaging temps réel)
- **front** : Nginx Alpine  (Serveur web + reverse proxy)
- **phpmyadmin** : Interface SQL  (http://localhost:8888)
- **mongo-express** : Interface NoSQL  (http://localhost:8889)
- **wsapi** : Spring Boot 3.3.3 (API en temps réel)
- **restapi** : Python Flask 3.0 (API rest)

###  Identifiants Configurés
| Service | Utilisateur | Mot de passe | Base/Queue |
|---------|-------------|-------------|------------|
| **MariaDB root** | `root` | `logistico_root_2024` | - |
| **MariaDB app** | `logistico_user` | `logistico_pass_2024` | `logistico_production` |
| **MongoDB** | `logistico_admin` | `mongo_pass_2024` | `logistico_history` |
| **RabbitMQ** | `rabbitmq_user` | `rabbitmq_pass_2024` | - |

(rabbitMQ n'utilise pas les secrets)
---

##  Guide de Demarrage Rapide

```powershell
# Demarrer tous les services (le build React se fait automatiquement)
docker-compose up -d

# Verifier le statut
docker-compose ps

# Acceder a l'application
# http://localhost - Application principale
# http://localhost:8888 - phpMyAdmin (root/logistico_root_2024)
# http://localhost:8889 - Mongo Express (logistico_admin/mongo_pass_2024)
# http://localhost:15672 - RabbitMQ Management (rabbitmq_user/rabbitmq_pass_2024)
```

---

##  Architecture Détaillée

### Étapes principales

1. **Préparation** : Configurer les secrets et volumes (cf. sections "Secrets" et "Volumes persistants")
2. **Déploiement** : Builder la webapp et les api puis démarrer les services principaux

# Description

##  Services Docker Compose

### 1. ProductionDB (Base de données de production)
**Service** : `sqldatabase`
- **Image** : MariaDB 11
- **Usage** : Stockage de l'état actuel du centre (rames, voies, tâches)
- **Volumes** :
  - `sqldata` : Stockage persistant des données (/var/lib/mysql)
  - Script d'initialisation : `init.sql` (création des tables au premier démarrage, mount by en lecture seulement)
- **Réseaux** :
  - `sql-net` : Réseau isolé pour accès aux bases de données
- **Configuration** :
  - Variables d'environnement pour les credentials ( MARIADB_ROOT_PASSWORD, MARIADB_DATABASE, MARIADB_USER, MARIADB_PASSWORD)
  - Healthcheck : vérification toutes les 10 s
  - Restart policy : `unless-stopped`
- **Secret** :
  - Credentials mysql (username/password)
  
---

### 2. HistoryDB (Base de données d'historique)
**Service** : `nosqldatabase`
- **Image** : MongoDB 7
- **Usage** : Stockage de l'historique de toutes les actions (demandes, entrées, sorties, tâches)
- **Volumes** :
  - `nosqldata` : Stockage persistant des documents (/data/db)
  - `nosqlconfig` : Stocke la configuration du serveur Mongo
- **Réseaux** :
  - `nosql-net` : Réseau isolé pour accès aux bases de données (même réseau que sqldatabase)
- **Configuration** :
  - Variables d'environnement pour les credentials ( MONGO_INITDB_ROOT_USERNAME, MONGO_INITDB_ROOT_PASSWORD, MONGO_INITDB_DATABASE)
  - Healthcheck : vérification toutes les 10 s
  - Restart policy : `unless-stopped`
- **Secrets** :
  - Credentials MongoDB (username/password)

---

### 3. MOM Broker (Message Broker temps réel)
**Service** : `broker`
- **Image** : `rabbitmq:management`
- **Usage** : Gestion des notifications temps réel entre conducteurs et opérateurs
- **Volumes** :
  - *brokerdata* : volume optionnel ajouté pour persister les queues (/var/lib/rabbitmq)
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
  - Credentials RabbitMQ (username/password) (Non fonctionnel)

---

### 4. REST API (API de gestion)
**Service** : `restapi`
- **Image** : DockerFile basé sur la dernière version de Python
- **Usage** : API REST pour consultation d'état, gestion des voies, inscription de tâches
- **Build** : Dockerfile avec code précompilé (base stable)
- **Volumes** : Aucun (code embarqué dans l'image pour optimisation)
- **Réseaux** :
  - `sql-net` : Accès aux bases de données
  - `nosql-net` : Accès aux bases de données
  - `front-net` : Communication avec front (reverse proxy)
- **Dépendances** :
  - sqldatabase (condition: service_healthy)
  - nosqldatabase (condition: service_healthy)
- **Configuration** :
  - Variables d'environnement pour la connection au bdd (DATABASE_URL , MONGODB_URL)
  - Restart policy : `unless-stopped`
- **Secrets** :
  - Credentials bases de données (SQL + NoSQL)

---

### 5. RealTime API (API temps réel)
**Service** : `wsapi`
- **Image** : DockerFile baser sur maven:3.9.8-eclipse-temurin-21
- **Usage** : WebSocket pour demandes/acceptations/sorties de rames + notifications temps réel
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
  - Variables d'environnement pour la connection bdd (SPRING_DATA_MONGODB_URI)
  - Variables d'environnement pour brokker : (APP_BROKER_HOST, APP_BROKER_PORT, APP_BROKER_LOGIN, APP_BROKER_PASSWORD)
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
  - Configuration Nginx personnalisée : ./vendorConfigurations/nginx.conf (bind mount en lecture seule)
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
  - Code source monté : ./app (bind mount)
  - `webapp-build` : Sortie du build - partagé avec front
- **Réseaux** :
  - Bridge par défaut (pas besoin de réseau custom, pas de communication avec autres services)
- **Configuration** :
  - Command : npm install ou npm ci && npm run build (install ou ci en fonction de l'hôte)
  - Fichier de configuration : webpack.prod.js

- **Note** : utiliser npm install ou ci en fonction de l'os hôte

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
  - Variables d'environnement (ME_CONFIG_MONGODB_SERVER, ME_CONFIG_MONGODB_PORT, ME_CONFIG_MONGODB_ADMINUSERNAME_FILE, ME_CONFIG_MONGODB_ADMINPASSWORD_FILE)
  - Restart policy : always (outil de développement disponible en permanence)
- **Secrets** :
  - Utilise les credentials de nosqldatabase

---

##  Topologie réseau

### Réseaux définis

- **`sql-net`** : Réseau isolé pour bases de données
  - Membres : sqldatabase, restapi, wsapi, phpmyadmin
  - driver : bridge

- **`nosql-net`** : Réseau isolé pour bases de données
  - Membres : nosqldatabase, restapi, wsapi, mongo-express
  - driver : bridge

- **`broker-net`** : Réseau isolé pour message broker
  - Membres : broker, wsapi
  - driver : bridge

- **`front-net`** : Réseau frontend-backend
  - Membres : restapi, wsapi, front
  - driver : bridge

- **Bridge par défaut** : Pour webapp (pas de communication avec autres services)

---

##  Commandes de déploiement

```powershell
# Lancer les services principaux
docker compose up -d

# Lancer avec outils de développement
docker compose --profile dev-tool up -d

# Arrêter tous les services
docker compose down

# Voir les logs
docker compose logs -f [service]
```

---

## 🚀 Guide d'exécution manuel complet

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
docker-compose run webapp

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

##  Problèmes Résolus et Solutions


###  Erreurs Hibernate "Schema-validation: missing column [num_serie]" (RÉSOLU)
**Problème** : Spring Boot ne trouvait pas la colonne `num_serie` dans la table `taches`

**Diagnostic** : Incohérence entre entités Java et schéma BDD
- Entité Java `Tache.java` : `@JoinColumn(name = "num_serie")`  
- BDD `init.sql` : Table avec colonne `num_serie_rame`

**Solution appliquée** :
```sql
-- init-db/init.sql modifié
CREATE TABLE taches (
    num_tache INT NOT NULL,
    num_serie VARCHAR(12) NOT NULL,  -- Corrigé : était num_serie_rame
    tache TEXT,
    PRIMARY KEY (num_tache, num_serie),
    FOREIGN KEY (num_serie) REFERENCES rames(num_serie)
);
```

###  Volumes Docker anonymes (RÉSOLU)
**Problème** : `docker-compose up` créait un volume anonyme non nommé

**Solution** : Tous les volumes nommés et documentés dans `docker-compose.yaml`
```yaml
volumes:
  brokerdata:        # Données RabbitMQ (/var/lib/rabbitmq)
```

###  Erreur Nginx 500 "rewrite or internal redirection cycle" (RÉSOLU)
**Problème** : Boucle de redirection infinie sur toutes les pages

**Cause** : Configuration Nginx incohérente
- Volume monté : `/var/www/app` 
- Mais config manquait : `location / { try_files $uri $uri/ /index.html; }`

**Solution** :
```nginx
# vendorConfigurations/nginx.conf
location / {
    try_files $uri $uri/ /index.html;
}
```

###  Authentification MongoDB (RÉSOLU)
**Problème** : Erreur "AuthenticationFailed" pour Spring Boot

**Solution** : Configuration complète avec base d'authentification
```properties
# application-development.properties
spring.data.mongodb.uri=mongodb://logistico_admin:mongo_pass_2024@nosqldatabase:27017/logistico_history?authSource=admin
```

###  Erreur de build de l'api realtime (RÉSOLU)
**Problème** : Erreur lors de la compilation de l'api Realtime
**Cause** : Mauvais choix de la version maven
**Solution appliquée** : Passer maven à la version supérieur

### Erreur de compilation de l'application front-end (PARTIELLEMENT RÉSOLU)
**Problème** : Erreur lors de l'execution de npm install dans le service webapp
**Cause** : Selon quelque chose, pendant l'installation, des dossiers se retrouvait avec un nom modifier ce qui bloquait npm (La source du problème reste inconu)
**Solution appliquée** : Utilise npm ci au lieu de npm i (Cette solution n'est pas correcte)

---

##  Configuration Avancée

### Variables d'Environnement par Service

#### RESTApi (Python Flask)
```bash
DATABASE_URL=mysql://logistico_user:logistico_pass_2024@sqldatabase:3306/logistico_production
MONGODB_URL=mongodb://logistico_admin:mongo_pass_2024@nosqldatabase:27017/logistico_history?authSource=admin
ENABLE_CORS=true
DEBUG=false
```

#### WSApi (Spring Boot)  
```properties
# Profil actif : development (charge application-development.properties)
spring.profiles.active=development
spring.datasource.url=jdbc:mariadb://sqldatabase:3306/logistico_production
spring.data.mongodb.uri=mongodb://logistico_admin:mongo_pass_2024@nosqldatabase:27017/logistico_history?authSource=admin
app.broker.host=broker
```

### Configuration Réseau Nginx
```nginx
# REST API proxy
location ~* ^/api {
    proxy_pass http://restapi:5000;
}

# WebSocket API proxy  
location ~* ^/wsapi {
    rewrite /wsapi/?(.*) /api/$1 break;
    proxy_pass http://wsapi:8080;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection $connection_upgrade;
}

# Frontend SPA
location / {
    try_files $uri $uri/ /index.html;
}
```
