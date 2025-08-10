#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
   CREATE DATABASE "ECommerce-Order-Service";
   CREATE DATABASE "ECommerce-Product-Service";
   CREATE DATABASE "ECommerce-Inventory-Service";

   GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Order-Service" TO "$POSTGRES_USER";
   GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Product-Service" TO "$POSTGRES_USER";
   GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Inventory-Service" TO "$POSTGRES_USER";

EOSQL
echo "Successfully initialized multiple databases."