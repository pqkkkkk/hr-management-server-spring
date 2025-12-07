-- Convert PostgreSQL enum types to VARCHAR with CHECK constraints
-- This resolves the Hibernate/JPA compatibility issue with custom enum types

-- First, drop existing CHECK constraints that reference enum types
ALTER TABLE request_table DROP CONSTRAINT IF EXISTS check_reject_reason;
ALTER TABLE request_table DROP CONSTRAINT IF EXISTS check_processed_at;

-- Convert request_table columns
ALTER TABLE request_table 
    ALTER COLUMN request_type TYPE VARCHAR(50),
    ALTER COLUMN status TYPE VARCHAR(50);

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

-- Add check constraints to enforce valid values
ALTER TABLE request_table
    ADD CONSTRAINT check_request_type_values 
    CHECK (request_type IN ('CHECK_IN', 'CHECK_OUT', 'TIMESHEET', 'LEAVE', 'WFH'));

ALTER TABLE request_table
    ADD CONSTRAINT check_status_values 
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'PROCESSING'));

-- Convert additional_leave_info_table
ALTER TABLE additional_leave_info_table 
    ALTER COLUMN leave_type TYPE VARCHAR(50);

ALTER TABLE additional_leave_info_table
    ADD CONSTRAINT check_leave_type_values 
    CHECK (leave_type IN ('ANNUAL', 'SICK', 'UNPAID', 'MATERNITY', 'PATERNITY', 'BEREAVEMENT', 'MARRIAGE', 'OTHER'));

-- Convert leave_dates_table
ALTER TABLE leave_dates_table 
    ALTER COLUMN shift TYPE VARCHAR(50);

ALTER TABLE leave_dates_table
    ADD CONSTRAINT check_shift_values 
    CHECK (shift IN ('FULL_DAY', 'MORNING', 'AFTERNOON'));

-- Convert wfh_dates_table
ALTER TABLE wfh_dates_table 
    ALTER COLUMN shift TYPE VARCHAR(50);

ALTER TABLE wfh_dates_table
    ADD CONSTRAINT check_wfh_shift_values 
    CHECK (shift IN ('FULL_DAY', 'MORNING', 'AFTERNOON'));

-- Drop the old enum types (they're no longer used by any tables)
DROP TYPE IF EXISTS request_type CASCADE;
DROP TYPE IF EXISTS request_status CASCADE;
DROP TYPE IF EXISTS leave_type CASCADE;
DROP TYPE IF EXISTS shift_type CASCADE;
