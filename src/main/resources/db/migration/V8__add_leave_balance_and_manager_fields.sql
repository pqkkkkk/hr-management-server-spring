-- Add leave balance, WFH days, and manager relationship to user_table

-- Add manager relationship (self-referencing)
ALTER TABLE user_table ADD COLUMN manager_id VARCHAR(36);
ALTER TABLE user_table ADD CONSTRAINT fk_user_manager 
    FOREIGN KEY (manager_id) REFERENCES user_table(user_id);

-- Add annual leave balance fields
ALTER TABLE user_table ADD COLUMN max_annual_leave INTEGER DEFAULT 12;
ALTER TABLE user_table ADD COLUMN remaining_annual_leave DECIMAL(4,1) DEFAULT 12.0;

-- Add WFH days balance fields
ALTER TABLE user_table ADD COLUMN max_wfh_days INTEGER DEFAULT 0;
ALTER TABLE user_table ADD COLUMN remaining_wfh_days INTEGER DEFAULT 0;

-- Create index for manager lookup
CREATE INDEX idx_user_manager ON user_table(manager_id);

-- Update existing users with default values
UPDATE user_table SET 
    max_annual_leave = 12,
    remaining_annual_leave = 12.0,
    max_wfh_days = 0,
    remaining_wfh_days = 0
WHERE max_annual_leave IS NULL;

-- Update manager relationships for existing sample data
-- Assuming manager user ID from sample data: 'u2b3c4d5-f6a7-8901-bcde-f12345678901'
UPDATE user_table 
SET manager_id = 'u2b3c4d5-f6a7-8901-bcde-f12345678901'
WHERE user_id != 'u2b3c4d5-f6a7-8901-bcde-f12345678901' 
    AND role != 'HR'
    AND role != 'ADMIN';
