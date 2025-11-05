# BASE SERVER CONFIGURATION
# General
# use 0.0.0.0:5000 for a docker deployment
SERVER_HOST = '0.0.0.0'
SERVER_PORT = 5000
DEBUG = False
# CORS Configuration
ENABLE_CORS = True  # Enable CORS compliancy only if the front app is served by another server (mostly in dev. conf)

# Helper function to read Docker secrets
def read_secret(secret_name):
    try:
        with open(f'/run/secrets/{secret_name}', 'r') as f:
            return f.read().strip()
    except:
        return None

# SQL PRODUCTION DB CONNECTION CONFIGURATION
SQLDB_SETTINGS = {
    "db": 'logistico_production',  # mandatory
    "user": read_secret('mysql_user'),  # mandatory
    "password": read_secret('mysql_password'),  # mandatory
    "host": 'sqldatabase',  # Docker service name
    "port": 3306  # default 3306
}

# MONGODB HISOTRY DB CONNECTION CONFIGURATION
MONGODB_SETTINGS = {
    "db": "logistico_history",  # Mandatory
    "host": "nosqldatabase",  # Docker service name
    "port": 27017,  # default 27017
    "username": read_secret('mongo_root_username'),  # Optional
    "password": read_secret('mongo_root_password'),  # Optional
    "authentication_source": "admin"  # default is the db
}
