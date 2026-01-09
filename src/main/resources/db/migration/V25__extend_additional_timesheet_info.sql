-- V25__extend_additional_timesheet_info.sql
-- Add new columns for comprehensive timesheet update support

-- H2 compatible syntax: separate ALTER statements for each column
ALTER TABLE additional_timesheet_info_table
ADD COLUMN desired_morning_status VARCHAR(20);

ALTER TABLE additional_timesheet_info_table
ADD COLUMN desired_afternoon_status VARCHAR(20);

ALTER TABLE additional_timesheet_info_table
ADD COLUMN desired_morning_wfh BOOLEAN;

ALTER TABLE additional_timesheet_info_table
ADD COLUMN desired_afternoon_wfh BOOLEAN;

-- Update nullable constraints for check times (they may not always be required)
-- Use PostgreSQL compatible syntax (DROP NOT NULL)
ALTER TABLE additional_timesheet_info_table
ALTER COLUMN desired_check_in_time
DROP NOT NULL;

ALTER TABLE additional_timesheet_info_table
ALTER COLUMN desired_check_out_time
DROP NOT NULL;

ALTER TABLE additional_timesheet_info_table
ALTER COLUMN current_check_in_time
DROP NOT NULL;

ALTER TABLE additional_timesheet_info_table
ALTER COLUMN current_check_out_time
DROP NOT NULL;