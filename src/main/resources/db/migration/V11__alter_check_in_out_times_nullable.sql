-- Alter additional_checkin_info_table and additional_checkout_info_table
-- to make current_check_in_time and current_check_out_time nullable
-- This allows flexibility when creating check-in/check-out requests

-- Drop constraints first
ALTER TABLE additional_checkin_info_table DROP CONSTRAINT IF EXISTS check_checkin_time_validity;
ALTER TABLE additional_checkout_info_table DROP CONSTRAINT IF EXISTS check_checkout_time_validity;

-- Make current_check_in_time nullable
ALTER TABLE additional_checkin_info_table ALTER COLUMN current_check_in_time DROP NOT NULL;

-- Make current_check_out_time nullable
ALTER TABLE additional_checkout_info_table ALTER COLUMN current_check_out_time DROP NOT NULL;
