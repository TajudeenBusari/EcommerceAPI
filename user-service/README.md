The user-service module is responsible for managing user-related operations within the application. 
It provides functionalities such as user registration, authentication, profile management, and password recovery.
It is based on Reactive webflux framework to handle asynchronous data streams efficiently, so the
dependencies are designed accordingly and are different from traditional Spring MVC based modules (like inventory-service etc.).
When the application starts, it does not automatically create DB TABLE because it does not use the spring.jpa.hibernate.ddl-auto property.
You need to create the necessary tables manually in the database before running the application or use a database migration tool like Flyway
or Liquibase to manage your database schema.
----------------------------------------------------------------------------------------------
OPTION1: Manually create the necessary tables in the database before running the application.
OPTION2: Use a database migration tool like Flyway or Liquibase to manage your database schema
         add migration scripts in, for example, 
               **src/main/resources/db/migration folder**.
               V1__create_users_table.sql
               Flyway runs on startup (blocking JDBC) and ensures that table exits, works with reactive R2DBC.
               SAMPLE CONTENT:
                --------------------------------------------------------------------------                       
                CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL,
                password VARCHAR(255) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT now(),
                updated_at TIMESTAMP NOT NULL DEFAULT now()
                );
                
                -- Optional: trigger to update 'updated_at' on row update
                CREATE OR REPLACE FUNCTION update_updated_at_column()
                RETURNS TRIGGER AS $$
                BEGIN
                NEW.updated_at = now();
                RETURN NEW;
                END;
                $$ language 'plpgsql';
                
                CREATE TRIGGER update_users_updated_at
                BEFORE UPDATE ON users
                FOR EACH ROW
                EXECUTE FUNCTION update_updated_at_column();
                -----------------------------------------------------------------------------------------------------------------
        


It is advisable not to make direct changes in the .sql scripts else it may lead to inconsistencies in the database schema management.
In this case, you will have to delete the database and re-create it again to ensure that the migration scripts are applied correctly from scratch.
