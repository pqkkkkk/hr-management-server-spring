-- Change WFH days balance fields from INTEGER to DECIMAL to support half days

-- Change max_wfh_days from INTEGER to DECIMAL(4,1)
ALTER TABLE user_table ALTER COLUMN max_wfh_days TYPE DECIMAL(4, 1);

-- Change remaining_wfh_days from INTEGER to DECIMAL(4,1)
ALTER TABLE user_table
ALTER COLUMN remaining_wfh_days TYPE DECIMAL(4, 1);

-- Update default values for existing users (if any have NULL values)
UPDATE user_table
SET
    max_wfh_days = 10.0,
    remaining_wfh_days = 10.0
WHERE
    max_wfh_days IS NULL
    OR remaining_wfh_days IS NULL;