-- Migration to remove notifications with reference_type = 'SYSTEM'
-- These were incorrectly added in V21 as the Java enum does not include SYSTEM as a valid value

DELETE FROM notification_table WHERE reference_type = 'SYSTEM';