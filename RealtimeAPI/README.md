# LogisticoTrain - RealTime API

Websocket/REST based API to handle Rame entrance request/answer and removal for the LogisticoTrain system

## Requirements
- Java JDK ≥ 21 (Amazon Corretto recommended) 
- Maven ≥ 3.1
- A SQL Database (Mariadb ≥ 11 recommended), with the production schema created in the database (cf.: [Relational production schema](##Relational-production-schema))
- A Mongodb database (MongDb ≥ 4 recommended). No collection setup is required; the application will set up the indexes automatically.
- A Broker that allows STOMP Protocol (RabbitMQ ≥ 3.1 with stomp and stomp_web plugins recommended, cf.: [Exemple of a RabbitMQ configuration](##Exemple-of-a-RabbitMQ-configuration))


## Configuration

Change the application properties in `./src/main/resources/application.property` (or copy it somewhere else and modify its copy if you do not want to modify the original source cours) according to your environment:
    - Global server configuration
      - for production, do not forget to remove applications profiles (do not use "development")
    - Production database configuration (SQL database)
    - History database configuration (MongoDb database)
    - Broker configuration (RabbitMq)

## Application launch

If you have directly edited the application.property file in src/main/resources, you can compile and launch the server with this command :
```
mvn spring-boot:run
```

If your properties file is located somewhere else, you can provide its location to the command:
```
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=<path to you application.properties file>
```

For any reason, If you need several properties files , you can provide their location with the following command:
```
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=<path to you application.properties file>,--spring.config.additional-location=<path to your other properties file>
```

## Relational production schema

The SQL database must have the given schema and constraints.

### Table __voie__

| Field     | Type       | Null | Key | Default | Extra |
+-----------+------------+------+-----+---------+-------+
| num_voie  | int(11)    | NO   | PRI | NULL    |       |
| interdite | tinyint(1) | NO   |     | NULL    |       |

__Constraints :__
- Primary Key (num_voie)

### Table __rames__

| Field              | Type        | Null | Key | Default | Extra |
+--------------------+-------------+------+-----+---------+-------+
| num_serie          | varchar(12) | NO   | PRI | NULL    |       |
| type_rame          | varchar(50) | NO   |     | NULL    |       |
| voie               | int(11)     | YES  | UNI | NULL    |       |
| conducteur_entrant | varchar(50) | NO   |     | NULL    |       |

__Constraints :__
- Primary Key (num_serie)
- Unique (voie)
- Foreign Key (voie) references Voie (num_voie)

### Table __taches__

| Field     | Type        | Null | Key | Default | Extra |
+-----------+-------------+------+-----+---------+-------+
| num_serie | varchar(12) | NO   | PRI | NULL    |       |
| num_tache | int(11)     | NO   | PRI | NULL    |       |
| tache     | text        | NO   |     | NULL    |       |

__Constraints :__
- Primary Key (num_serie, num_tache)
- Foreign Key (num_serie) references rames (num_serie)

## Exemple of a RabbitMQ configuration

The application requires a message broker that can communicate using the STOMP protocol.

Using RabbitMQ, we can use the following command to enable the stomp plugin:
```
rabbitmq-plugins enable rabbitmq_stomp
```

You will also need to update your rabbitmq configuration with the two folling properties

```
stomp.default_user = <broker username>
stomp.default_pass = <borker password>
```

_Note_: avoid modifing the /etc/rabbitmq/rabbitmq.conf directly, you rather prefer creating a specific configuration file for stomp in the /etc/rabbitmq/conf.d/ directory (e.g.: /etc/rabbitmq/conf.d/30-stomp.conf), with the above configuration.
