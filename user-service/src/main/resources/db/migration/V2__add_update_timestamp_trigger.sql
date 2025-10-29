
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
   --only create the trigger if the users table exists---
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        --use EXECUTE to avoid already exist errors---
        IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_users_updated_at')
        THEN
        EXECUTE 'CREATE TRIGGER update_users_updated_at
                 BEFORE UPDATE ON users
                 FOR EACH ROW
                 EXECUTE FUNCTION update_updated_at_column();';
        END IF;
    END IF;
END $$;


