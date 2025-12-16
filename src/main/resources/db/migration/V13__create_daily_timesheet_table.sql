-- Create daily_timesheet table
CREATE TABLE daily_timesheet_table
(
    daily_ts_id          VARCHAR(255) PRIMARY KEY,
    date                 DATE         NOT NULL,
    morning_status       VARCHAR(50),
    afternoon_status     VARCHAR(50),
    morning_wfh          BOOLEAN,
    afternoon_wfh        BOOLEAN,
    total_work_credit    DOUBLE PRECISION,
    check_in_time        TIMESTAMP,
    check_out_time       TIMESTAMP,
    late_minutes         INTEGER,
    early_leave_minutes  INTEGER,
    overtime_minutes     INTEGER,
    is_finalized         BOOLEAN,
    employee_id          VARCHAR(255) NOT NULL,
    CONSTRAINT fk_daily_timesheet_employee FOREIGN KEY (employee_id) REFERENCES user_table (user_id) ON DELETE CASCADE
);

-- Create index on employee_id for better query performance
CREATE INDEX idx_daily_timesheet_employee_id ON daily_timesheet_table (employee_id);

-- Create unique constraint to prevent duplicate timesheets for the same employee on the same date
CREATE UNIQUE INDEX idx_daily_timesheet_employee_date ON daily_timesheet_table (employee_id, date);

-- Add comments to the table and columns
COMMENT ON TABLE daily_timesheet_table IS 'Daily timesheet records for employees';
COMMENT ON COLUMN daily_timesheet_table.daily_ts_id IS 'Primary key - UUID';
COMMENT ON COLUMN daily_timesheet_table.date IS 'Date of the timesheet';
COMMENT ON COLUMN daily_timesheet_table.morning_status IS 'Attendance status for morning session (PRESENT, ABSENT, ON_LEAVE, LATE, EARLY_LEAVE)';
COMMENT ON COLUMN daily_timesheet_table.afternoon_status IS 'Attendance status for afternoon session (PRESENT, ABSENT, ON_LEAVE, LATE, EARLY_LEAVE)';
COMMENT ON COLUMN daily_timesheet_table.morning_wfh IS 'Whether employee worked from home in morning';
COMMENT ON COLUMN daily_timesheet_table.afternoon_wfh IS 'Whether employee worked from home in afternoon';
COMMENT ON COLUMN daily_timesheet_table.total_work_credit IS 'Total work credit for the day';
COMMENT ON COLUMN daily_timesheet_table.check_in_time IS 'Time when employee checked in';
COMMENT ON COLUMN daily_timesheet_table.check_out_time IS 'Time when employee checked out';
COMMENT ON COLUMN daily_timesheet_table.late_minutes IS 'Number of minutes employee was late';
COMMENT ON COLUMN daily_timesheet_table.early_leave_minutes IS 'Number of minutes employee left early';
COMMENT ON COLUMN daily_timesheet_table.overtime_minutes IS 'Number of minutes employee worked overtime';
COMMENT ON COLUMN daily_timesheet_table.is_finalized IS 'Whether the timesheet has been finalized';
COMMENT ON COLUMN daily_timesheet_table.employee_id IS 'Foreign key to user_table';