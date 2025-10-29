# TODO Liste - Projet Logistico-Train

## Tâches accomplies

- [x] **README corrigé** - Corriger README avec orthographe et structure
- [x] **Docker-compose créé** - Créer docker-compose.yaml avec 9 services
- [x] **Secrets complets** - Tous fichiers secrets créés (MySQL, MongoDB, RabbitMQ)
- [x] **Script SQL** - Script init.sql créé avec tables et données test
- [x] **Dockerfile REST** - Dockerfile Python créé pour RESTApi
- [x] **Configuration réseaux** - 4 réseaux Docker configurés et testés
- [x] **Tests bases données** - SQL (MariaDB) et NoSQL (MongoDB) opérationnels
- [x] **Test RabbitMQ** - Message broker fonctionnel avec management UI
- [x] **Outils admin** - phpMyAdmin et Mongo Express déployés

## Tâches restantes

- [ ] **Corriger API REST** - Réparer redémarrage boucle API Python
- [ ] **Corriger API Spring Boot** - Résoudre erreur code 127 WebSocket API
- [ ] **Upgrade Spring Boot** - Mise à jour 3.3.3 vers 3.5.x
- [ ] **Validation stack complète** - Test end-to-end complet
- [ ] **Documentation finale** - Commit configuration réseaux/secrets/volumes

## Questions d'évaluation possibles

### 🏗️ Architecture et Conception

**Q: Expliquez l'architecture multi-services de votre application**
- **Réponse** : 9 services déployés avec séparation claire des responsabilités
  - Couche données : MariaDB (SQL) + MongoDB (NoSQL) 
  - Couche messaging : RabbitMQ pour communication asynchrone
  - Couche API : REST Python + WebSocket Spring Boot
  - Couche présentation : React SPA + Nginx reverse proxy
  - Outils dev : phpMyAdmin + Mongo Express

**Q: Justifiez le choix d'une base polyglotte (SQL + NoSQL)**
- **Réponse** : 
  - MariaDB : Données relationnelles (utilisateurs, trains, voies) avec contraintes ACID
  - MongoDB : Documents JSON (logs, historiques) avec flexibilité schéma
  - Permet d'optimiser chaque type de données selon ses besoins

**Q: Pourquoi utiliser Docker Compose plutôt que Kubernetes ?**
- **Réponse** : Environnement de développement local, simplicité de déploiement, gestion des dépendances entre services, idéal pour prototypage et tests

### 🔒 Sécurité et Réseaux

**Q: Comment avez-vous sécurisé les communications entre services ?**
- **Réponse** : 4 réseaux Docker isolés
  - `sql-net` : Bases + APIs + outils admin uniquement
  - `broker-net` : RabbitMQ + WebSocket API  
  - `front-net` : Frontend + APIs backend
  - Isolation empêche accès non autorisés entre couches

**Q: Expliquez votre stratégie de gestion des secrets**
- **Réponse** : Docker Secrets avec fichiers externes
  - Credentials dans `secrets/` (à exclure du git)
  - Variables d'environnement pointent vers secrets
  - Rotation possible sans rebuild images

**Q: Pourquoi exposer les outils admin uniquement sur localhost ?**
- **Réponse** : Sécurité - `127.0.0.1:8888/8889` empêche accès externe, outils sensibles protégés

### 🐛 Résolution de Problèmes

**Q: Décrivez un problème technique majeur rencontré et sa résolution**
- **Réponse** : Erreur 500 Nginx "rewrite or internal redirection cycle"
  - **Cause** : Volume monté sur `/usr/share/nginx/html` mais config nginx pointait vers `/var/www/app`
  - **Diagnostic** : Logs Nginx + inspection volumes Docker
  - **Solution** : Correction montage volume + copie manuelle fichiers React
  - **Apprentissage** : Importance cohérence configuration/montages

**Q: Comment debugguer un service qui redémarre en boucle ?**
- **Réponse** : 
  1. `docker-compose logs [service]` pour logs d'erreur
  2. `docker-compose ps` pour codes de sortie  
  3. `docker exec -it [container] sh` pour inspection interne
  4. Vérification dépendances, variables d'environnement, health checks

### ⚙️ DevOps et Outils

**Q: Expliquez votre processus de build de l'application React**
- **Réponse** : Service `webapp` dédié
  - Webpack en mode production pour optimisation
  - Volume nommé `webapp-build` partagé avec Nginx
  - Séparation build/serving pour meilleure architecture

**Q: Comment gérez-vous les dépendances entre services ?**
- **Réponse** : `depends_on` + health checks
  - APIs attendent que bases soient "healthy"
  - Frontend attend APIs backend
  - Évite erreurs de connexion au démarrage

**Q: Que feriez-vous pour passer en production ?**
- **Réponse** : 
  - Secrets management sécurisé (HashiCorp Vault)
  - Load balancer + multiple instances
  - Monitoring (Prometheus/Grafana)
  - CI/CD pipeline
  - HTTPS/TLS certificats
  - Backup stratégie
  - Log aggregation (ELK Stack)

## Progression

**Avancement :** 10/14 tâches (71%)

**Statut conteneurs :** 6/7 opérationnels (SQL, NoSQL, RabbitMQ, Frontend, phpMyAdmin, Mongo Express)

**Prochaine étape :** Réparer APIs qui redémarrent (REST Python + Spring Boot)