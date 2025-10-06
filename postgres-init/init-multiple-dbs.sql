
-- Create multiple databases for microservices
CREATE DATABASE "ECommerce-Order-Service";
CREATE DATABASE "ECommerce-Product-Service";
CREATE DATABASE "ECommerce-Inventory-Service";
CREATE DATABASE "ECommerce-Notification-Service";

-- Grant all privileges on the databases to the postgres user
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Order-Service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Product-Service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Inventory-Service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "ECommerce-Notification-Service" TO postgres;

-- print a message to indicate that the databases have been created
-- sql dialect is not configured statement can be ignored or just click use PostgreSQL
DO
$$
BEGIN
   RAISE NOTICE 'Databases for microservices created successfully!';
END;
$$;