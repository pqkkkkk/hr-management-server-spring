-- Update sample users with leave balance and manager relationships

-- Update manager (u2b3c4d5-f6a7-8901-bcde-f12345678901) with higher leave balance
UPDATE user_table 
SET 
    max_annual_leave = 15,
    remaining_annual_leave = 15.0,
    max_wfh_days = 12,
    remaining_wfh_days = 12,
    manager_id = NULL  -- Manager has no manager
WHERE user_id = 'u2b3c4d5-f6a7-8901-bcde-f12345678901';

-- Update HR and ADMIN roles with no manager and higher leave balance
UPDATE user_table 
SET 
    max_annual_leave = 15,
    remaining_annual_leave = 15.0,
    max_wfh_days = 12,
    remaining_wfh_days = 12,
    manager_id = NULL
WHERE role IN ('HR', 'ADMIN');

-- Update all other employees with standard leave balance and assign manager
UPDATE user_table 
SET 
    max_annual_leave = 12,
    remaining_annual_leave = 12.0,
    max_wfh_days = 6,
    remaining_wfh_days = 6,
    manager_id = 'u2b3c4d5-f6a7-8901-bcde-f12345678901'
WHERE user_id != 'u2b3c4d5-f6a7-8901-bcde-f12345678901' 
  AND role NOT IN ('HR', 'ADMIN')
  AND manager_id IS NULL;
