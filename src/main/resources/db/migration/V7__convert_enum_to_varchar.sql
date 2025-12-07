-- Convert PostgreSQL enum types to VARCHAR with CHECK constraints
-- This resolves the Hibernate/JPA compatibility issue with custom enum types

-- Drop existing constraints from V4 migration (these definitely exist)
ALTER TABLE request_table DROP CONSTRAINT IF EXISTS check_reject_reason;
ALTER TABLE request_table DROP CONSTRAINT IF EXISTS check_processed_at;

-- Convert request_table columns (separate statements for H2 compatibility)
ALTER TABLE request_table ALTER COLUMN request_type TYPE VARCHAR(50);
ALTER TABLE request_table ALTER COLUMN status TYPE VARCHAR(50);

-- Re-add business rule constraints (now with VARCHAR)
ALTER TABLE request_table
    ADD CONSTRAINT check_reject_reason CHECK (
        (status = 'REJECTED' AND reject_reason IS NOT NULL) OR 
        (status != 'REJECTED' AND reject_reason IS NULL)
    );

ALTER TABLE request_table
    ADD CONSTRAINT check_processed_at CHECK (
        (status IN ('APPROVED', 'REJECTED') AND processed_at IS NOT NULL) OR 
        (status NOT IN ('APPROVED', 'REJECTED') AND processed_at IS NULL)
    );

-- Convert additional_leave_info_table
ALTER TABLE additional_leave_info_table 
    ALTER COLUMN leave_type TYPE VARCHAR(50);

-- Convert leave_dates_table
ALTER TABLE leave_dates_table 
    ALTER COLUMN shift TYPE VARCHAR(50);

-- Convert wfh_dates_table
ALTER TABLE wfh_dates_table 
    ALTER COLUMN shift TYPE VARCHAR(50);

-- Drop the old enum types (they're no longer used by any tables)
DROP TYPE IF EXISTS request_type CASCADE;
DROP TYPE IF EXISTS request_status CASCADE;
DROP TYPE IF EXISTS leave_type CASCADE;
DROP TYPE IF EXISTS shift_type CASCADE;
