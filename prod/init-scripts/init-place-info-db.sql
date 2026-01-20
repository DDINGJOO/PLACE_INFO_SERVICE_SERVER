-- ============================================
-- Place Info Service Database Initialization
-- PostgreSQL with PostGIS Extension
-- ============================================

-- Enable PostGIS extension (required for spatial queries)
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- ============================================
-- Note: Tables are managed by Hibernate/JPA
-- This script only initializes extensions and indexes
-- that cannot be managed by JPA annotations
-- ============================================

-- Create spatial index on place_locations if table exists
-- This will be executed after Hibernate creates the tables
DO $$
BEGIN
    -- Check if the table exists before creating index
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_locations') THEN
        -- Create GIST index for spatial queries if not exists
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_place_locations_coordinates_gist') THEN
            CREATE INDEX idx_place_locations_coordinates_gist ON place_locations USING GIST (coordinates);
        END IF;

        -- Create composite index for lat/lng queries
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_place_locations_lat_lng') THEN
            CREATE INDEX idx_place_locations_lat_lng ON place_locations (latitude, longitude);
        END IF;
    END IF;
END
$$;

-- ============================================
-- Performance Indexes (created after tables exist)
-- ============================================
DO $$
BEGIN
    -- Index for place_info search queries
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_info') THEN
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_place_info_status_rating') THEN
            CREATE INDEX idx_place_info_status_rating ON place_info (registration_status, rating DESC, created_at DESC);
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_place_info_user_id') THEN
            CREATE INDEX idx_place_info_user_id ON place_info (user_id);
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_place_info_approval_status') THEN
            CREATE INDEX idx_place_info_approval_status ON place_info (approval_status);
        END IF;
    END IF;

    -- Index for place_contacts email lookup
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_contacts') THEN
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_place_contacts_email') THEN
            CREATE INDEX idx_place_contacts_email ON place_contacts (email);
        END IF;
    END IF;
END
$$;

-- ============================================
-- Grant permissions (if using separate read user)
-- ============================================
-- Uncomment if you want to set up read-only user for slave
-- CREATE USER readonly_user WITH PASSWORD 'readonly_password';
-- GRANT CONNECT ON DATABASE place TO readonly_user;
-- GRANT USAGE ON SCHEMA public TO readonly_user;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly_user;

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'Place Info Service database initialization completed successfully';
END
$$;
