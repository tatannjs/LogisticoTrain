# LogisticoTrain - REST API

REST based API to handle Track, train and task for the LogisticoTrain system.

## Requirements
- Python ≥ 3.11
- mysqclient support (see https://github.com/PyMySQL/mysqlclient)
- A SQL Database (Mariadb ≥ 11 recommended), with the production schema created in the database (cf.: [Relational production schema](##Relational-production-schema))
- A Mongodb database (MongDb ≥ 4 recommended). No collection setup is required; the application will set up the indexes automatically.

## Installation

According to your environment you might want to use a virtual environment.

Install all requirements from requirements.txt (`pip install -r requirements.txt`).

## Configuration

Change the application properties in `./config.py` according to your environment:
    - Global server configuration
    - Production database configuration (SQL database)
    - History database configuration (MongoDb database)

## Application launch

You can either start the standalone application from a python environement:
```
python MyRamesServer.py [options]
```
or using the startup shell script:
```
sh start-server.sh [options]
```

## Relational production schema

The SQL database must have the given schema and constraints.

_Note_: each table must be created with the DEFAULT CHARSET utf8mb4 and the COLLATE utf8mb4_general_ci.

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