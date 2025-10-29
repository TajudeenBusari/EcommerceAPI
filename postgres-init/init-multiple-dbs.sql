
-- Create multiple databases for microservices
CREATE DATABASE "ECommerce-Order-Service";
CREATE DATABASE "ECommerce-Product-Service";
CREATE DATABASE "ECommerce-Inventory-Service";
CREATE DATABASE "ECommerce-Notification-Service";
CREATE DATABASE "ECommerce-User-Service";

-- Grant all privileges on the databases to the postgres user
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Order-Service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Product-Service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Inventory-Service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Notification-Service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-User-Service" TO postgres;

-- print a message to indicate that the databases have been created
-- sql dialect is not configured statement can be ignored or just click use PostgreSQL
DO
$$
BEGIN
   RAISE NOTICE 'Databases for microservices created successfully!';
END;
$$;

--sample batch script to create multiple databases in a PostgreSQL instance
--#bash script to create multiple databases in a PostgreSQL instance
--#causing issues with docker-entrypoint-initdb.d scripts, so it is currently not in use

-- ##!/bin/bash
-- #set -e
-- #
-- #psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
-- #   CREATE DATABASE "ECommerce-Order-Service";
-- #   CREATE DATABASE "ECommerce-Product-Service";
-- #   CREATE DATABASE "ECommerce-Inventory-Service";
-- #   CREATE DATABASE "ECommerce-Notification-Service";
-- #
-- #   GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Order-Service" TO "$POSTGRES_USER";
-- #   GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Product-Service" TO "$POSTGRES_USER";
-- #   GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Inventory-Service" TO "$POSTGRES_USER";
-- #   GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Notification-Service" TO "$POSTGRES_USER";
-- #
-- #EOSQL
-- #echo "Successfully initialized multiple databases."