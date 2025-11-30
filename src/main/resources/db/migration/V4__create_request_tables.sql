-- Create enum types for request module
DROP TYPE IF EXISTS request_type CASCADE;
DROP TYPE IF EXISTS request_status CASCADE;
DROP TYPE IF EXISTS leave_type CASCADE;
DROP TYPE IF EXISTS shift_type CASCADE;
CREATE TYPE request_type AS ENUM ('CHECK_IN', 'CHECK_OUT', 'TIMESHEET', 'LEAVE', 'WFH');
CREATE TYPE request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'PROCESSING');
CREATE TYPE leave_type AS ENUM ('ANNUAL', 'SICK', 'UNPAID', 'MATERNITY', 'PATERNITY', 'BEREAVEMENT', 'MARRIAGE', 'OTHER');
CREATE TYPE shift_type AS ENUM ('FULL_DAY', 'MORNING', 'AFTERNOON');

-- Main request table
CREATE TABLE request_table (
    request_id VARCHAR(36) PRIMARY KEY,
    request_type request_type NOT NULL,
    status request_status NOT NULL DEFAULT 'PENDING',
    title VARCHAR(255) NOT NULL,
    user_reason TEXT,
    reject_reason TEXT,
    attachment_url TEXT,
    employee_id VARCHAR(36) NOT NULL,
    approver_id VARCHAR(36),
    processor_id VARCHAR(36),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_request_table_employee FOREIGN KEY (employee_id) REFERENCES user_table(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_request_table_approver FOREIGN KEY (approver_id) REFERENCES user_table(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_request_table_processor FOREIGN KEY (processor_id) REFERENCES user_table(user_id) ON DELETE SET NULL,
    
    -- Business rules
    CONSTRAINT check_reject_reason CHECK (
        (status = 'REJECTED' AND reject_reason IS NOT NULL) OR 
        (status != 'REJECTED' AND reject_reason IS NULL)
    ),
    CONSTRAINT check_processed_at CHECK (
        (status IN ('APPROVED', 'REJECTED') AND processed_at IS NOT NULL) OR 
        (status NOT IN ('APPROVED', 'REJECTED') AND processed_at IS NULL)
    )
);

-- Additional CheckIn Info (1-1 relationship with request)
CREATE TABLE additional_checkin_info_table (
    request_id VARCHAR(36) PRIMARY KEY,
    desired_check_in_time TIMESTAMP NOT NULL,
    current_check_in_time TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_checkin_request FOREIGN KEY (request_id) REFERENCES request_table(request_id) ON DELETE CASCADE,
    CONSTRAINT check_checkin_time_validity CHECK (desired_check_in_time IS NOT NULL AND current_check_in_time IS NOT NULL)
);

-- Additional CheckOut Info (1-1 relationship with request)
CREATE TABLE additional_checkout_info_table (
    request_id VARCHAR(36) PRIMARY KEY,
    desired_check_out_time TIMESTAMP NOT NULL,
    current_check_out_time TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_checkout_request FOREIGN KEY (request_id) REFERENCES request_table(request_id) ON DELETE CASCADE,
    CONSTRAINT check_checkout_time_validity CHECK (desired_check_out_time IS NOT NULL AND current_check_out_time IS NOT NULL)
);

-- Additional Timesheet Info (1-1 relationship with request)
CREATE TABLE additional_timesheet_info_table (
    request_id VARCHAR(36) PRIMARY KEY,
    desired_check_in_time TIMESTAMP NOT NULL,
    current_check_in_time TIMESTAMP NOT NULL,
    desired_check_out_time TIMESTAMP NOT NULL,
    current_check_out_time TIMESTAMP NOT NULL,
    target_date DATE NOT NULL,
    
    CONSTRAINT fk_timesheet_request FOREIGN KEY (request_id) REFERENCES request_table(request_id) ON DELETE CASCADE,
    CONSTRAINT check_timesheet_time_order CHECK (
        current_check_in_time < current_check_out_time AND 
        desired_check_in_time < desired_check_out_time
    )
);

-- Additional Leave Info (1-1 relationship with request)
CREATE TABLE additional_leave_info_table (
    request_id VARCHAR(36) PRIMARY KEY,
    leave_type leave_type NOT NULL,
    total_days NUMERIC(4, 1) NOT NULL,
    
    CONSTRAINT fk_leave_request FOREIGN KEY (request_id) REFERENCES request_table(request_id) ON DELETE CASCADE,
    CONSTRAINT check_total_days_positive CHECK (total_days > 0)
);

-- Leave Dates (many-to-one with additional_leave_info)
CREATE TABLE leave_dates_table (
    leave_date_id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    shift shift_type NOT NULL DEFAULT 'FULL_DAY',
    
    CONSTRAINT fk_leave_dates_request FOREIGN KEY (request_id) REFERENCES additional_leave_info_table(request_id) ON DELETE CASCADE,
    CONSTRAINT unique_leave_date_per_request UNIQUE (request_id, date)
);

-- Additional WFH Info (1-1 relationship with request)
CREATE TABLE additional_wfh_info_table (
    request_id VARCHAR(36) PRIMARY KEY,
    wfh_commitment BOOLEAN NOT NULL DEFAULT false,
    work_location VARCHAR(500),
    total_days NUMERIC(4, 1) NOT NULL,
    
    CONSTRAINT fk_wfh_request FOREIGN KEY (request_id) REFERENCES request_table(request_id) ON DELETE CASCADE,
    CONSTRAINT check_wfh_total_days_positive CHECK (total_days > 0)
);

-- WFH Dates (many-to-one with additional_wfh_info)
CREATE TABLE wfh_dates_table (
    wfh_date_id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    shift shift_type NOT NULL DEFAULT 'FULL_DAY',
    
    CONSTRAINT fk_wfh_dates_request FOREIGN KEY (request_id) REFERENCES additional_wfh_info_table(request_id) ON DELETE CASCADE,
    CONSTRAINT unique_wfh_date_per_request UNIQUE (request_id, date)
);

-- Create indexes for better query performance
CREATE INDEX idx_request_status ON request_table(status);
CREATE INDEX idx_request_type ON request_table(request_type);
CREATE INDEX idx_request_employee ON request_table(employee_id);

-- Add comments for documentation
COMMENT ON TABLE request_table IS 'Main request table for all employee requests (check-in/out, timesheet, leave, WFH)';
COMMENT ON COLUMN request_table.status IS 'Current status: PENDING, APPROVED, REJECTED, CANCELLED, PROCESSING';
COMMENT ON COLUMN request_table.request_type IS 'Type of request: CHECK_IN, CHECK_OUT, TIMESHEET, LEAVE, WFH';
COMMENT ON COLUMN request_table.reject_reason IS 'Required when status is REJECTED, explains why request was rejected';
COMMENT ON COLUMN request_table.processed_at IS 'Timestamp when request was approved or rejected';

COMMENT ON TABLE additional_leave_info_table IS 'Additional information for leave requests';
COMMENT ON COLUMN additional_leave_info_table.total_days IS 'Total number of leave days (can be decimal for half days)';

COMMENT ON TABLE leave_dates_table IS 'Individual dates for leave requests, supports half-day leaves';
COMMENT ON COLUMN leave_dates_table.shift IS 'FULL_DAY, MORNING, or AFTERNOON for half-day leaves';

COMMENT ON TABLE additional_wfh_info_table IS 'Additional information for work-from-home requests';
COMMENT ON COLUMN additional_wfh_info_table.wfh_commitment IS 'Employee commitment to work from home conditions';
COMMENT ON COLUMN additional_wfh_info_table.total_days IS 'Total number of WFH days';

COMMENT ON TABLE wfh_dates_table IS 'Individual dates for WFH requests';
