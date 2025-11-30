-- Insert sample request data for testing

-- Sample CHECK_IN request (APPROVED)
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, employee_id, 
    approver_id, processor_id, processed_at, created_at, updated_at
) VALUES (
    'req-001', 'CHECK_IN', 'APPROVED', 
    'Late Check-in Request - Traffic Jam',
    'I was stuck in traffic due to an accident on the highway.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1),
    (SELECT user_id FROM user_table WHERE role = 'HR' LIMIT 1),
    (SELECT user_id FROM user_table WHERE role = 'HR' LIMIT 1),
    CURRENT_TIMESTAMP + CAST('-2 hours' AS INTERVAL),
    CURRENT_TIMESTAMP + CAST('-1 days' AS INTERVAL),
    CURRENT_TIMESTAMP + CAST('-2 hours' AS INTERVAL)
);

INSERT INTO additional_checkin_info_table (request_id, desired_check_in_time, current_check_in_time)
VALUES (
    'req-001',
    CURRENT_TIMESTAMP + CAST('-1 days' AS INTERVAL) + CAST('8 hours' AS INTERVAL),
    CURRENT_TIMESTAMP + CAST('-1 days' AS INTERVAL) + CAST('9 hours 30 minutes' AS INTERVAL)
);

-- Sample CHECK_OUT request (PENDING)
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, employee_id, created_at, updated_at
) VALUES (
    'req-002', 'CHECK_OUT', 'PENDING',
    'Early Check-out Request - Medical Appointment',
    'I have a doctor appointment at 4 PM.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1 OFFSET 1),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO additional_checkout_info_table (request_id, desired_check_out_time, current_check_out_time)
VALUES (
    'req-002',
    CAST(CURRENT_DATE AS TIMESTAMP) + CAST('17 hours' AS INTERVAL),
    CAST(CURRENT_DATE AS TIMESTAMP) + CAST('15 hours 30 minutes' AS INTERVAL)
);

-- Sample TIMESHEET request (APPROVED)
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, employee_id,
    approver_id, processor_id, processed_at, created_at, updated_at
) VALUES (
    'req-003', 'TIMESHEET', 'APPROVED',
    'Timesheet Correction - Forgot to Check-out',
    'I forgot to check out yesterday, please update my timesheet.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1),
    (SELECT user_id FROM user_table WHERE role = 'HR' LIMIT 1),
    (SELECT user_id FROM user_table WHERE role = 'HR' LIMIT 1),
    CURRENT_TIMESTAMP + CAST('-1 hours' AS INTERVAL),
    CURRENT_TIMESTAMP + CAST('-1 days' AS INTERVAL),
    CURRENT_TIMESTAMP + CAST('-1 hours' AS INTERVAL)
);

INSERT INTO additional_timesheet_info_table (
    request_id, desired_check_in_time, current_check_in_time,
    desired_check_out_time, current_check_out_time, target_date
) VALUES (
    'req-003',
    CAST(CURRENT_DATE AS TIMESTAMP) + CAST('-1 days' AS INTERVAL) + CAST('8 hours' AS INTERVAL),
    CAST(CURRENT_DATE AS TIMESTAMP) + CAST('-1 days' AS INTERVAL) + CAST('8 hours 15 minutes' AS INTERVAL),
    CAST(CURRENT_DATE AS TIMESTAMP) + CAST('-1 days' AS INTERVAL) + CAST('17 hours' AS INTERVAL),
    CAST(CURRENT_DATE AS TIMESTAMP) + CAST('-1 days' AS INTERVAL) + CAST('17 hours 30 minutes' AS INTERVAL),
    CURRENT_DATE + CAST('-1 days' AS INTERVAL)
);

-- Sample LEAVE request (PENDING) - 2.5 days
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, employee_id, created_at, updated_at
) VALUES (
    'req-004', 'LEAVE', 'PENDING',
    'Annual Leave - Family Vacation',
    'Planning a short family trip to Da Nang.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1 OFFSET 1),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO additional_leave_info_table (request_id, leave_type, total_days)
VALUES ('req-004', 'ANNUAL', 2.5);

INSERT INTO leave_dates_table (leave_date_id, request_id, date, shift) VALUES
    ('ld-001', 'req-004', CURRENT_DATE + CAST('7 days' AS INTERVAL), 'FULL_DAY'),
    ('ld-002', 'req-004', CURRENT_DATE + CAST('8 days' AS INTERVAL), 'FULL_DAY'),
    ('ld-003', 'req-004', CURRENT_DATE + CAST('9 days' AS INTERVAL), 'MORNING');

-- Sample LEAVE request (REJECTED)
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, reject_reason,
    employee_id, approver_id, processor_id, processed_at, created_at, updated_at
) VALUES (
    'req-005', 'LEAVE', 'REJECTED',
    'Sick Leave Request',
    'I am feeling unwell and need to rest.',
    'Please provide medical certificate for sick leave longer than 1 day.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1),
    (SELECT user_id FROM user_table WHERE role = 'HR' LIMIT 1),
    (SELECT user_id FROM user_table WHERE role = 'HR' LIMIT 1),
    CURRENT_TIMESTAMP + CAST('-30 minutes' AS INTERVAL),
    CURRENT_TIMESTAMP + CAST('-2 hours' AS INTERVAL),
    CURRENT_TIMESTAMP + CAST('-30 minutes' AS INTERVAL)
);

INSERT INTO additional_leave_info_table (request_id, leave_type, total_days)
VALUES ('req-005', 'SICK', 2.0);

INSERT INTO leave_dates_table (leave_date_id, request_id, date, shift) VALUES
    ('ld-004', 'req-005', CURRENT_DATE, 'FULL_DAY'),
    ('ld-005', 'req-005', CURRENT_DATE + CAST('1 days' AS INTERVAL), 'FULL_DAY');

-- Sample WFH request (APPROVED) - 3 days
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, employee_id,
    approver_id, processor_id, processed_at, attachment_url, created_at, updated_at
) VALUES (
    'req-006', 'WFH', 'APPROVED',
    'Work From Home - Home Renovation',
    'My house is under renovation and I need to supervise the workers.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1 OFFSET 1),
    (SELECT user_id FROM user_table WHERE role = 'MANAGER' LIMIT 1),
    (SELECT user_id FROM user_table WHERE role = 'MANAGER' LIMIT 1),
    CURRENT_TIMESTAMP + CAST('-3 hours' AS INTERVAL),
    'https://storage.example.com/attachments/renovation-schedule.pdf',
    CURRENT_TIMESTAMP + CAST('-2 days' AS INTERVAL),
    CURRENT_TIMESTAMP + CAST('-3 hours' AS INTERVAL)
);

INSERT INTO additional_wfh_info_table (request_id, wfh_commitment, work_location, total_days)
VALUES (
    'req-006',
    true,
    '123 Nguyen Hue Street, District 1, Ho Chi Minh City',
    3.0
);

INSERT INTO wfh_dates_table (wfh_date_id, request_id, date, shift) VALUES
    ('wfh-001', 'req-006', CURRENT_DATE + CAST('3 days' AS INTERVAL), 'FULL_DAY'),
    ('wfh-002', 'req-006', CURRENT_DATE + CAST('4 days' AS INTERVAL), 'FULL_DAY'),
    ('wfh-003', 'req-006', CURRENT_DATE + CAST('5 days' AS INTERVAL), 'FULL_DAY');

-- Sample WFH request (PENDING) - Half day
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, employee_id, created_at, updated_at
) VALUES (
    'req-007', 'WFH', 'PENDING',
    'Work From Home - Internet Technician Visit',
    'Internet technician is coming in the afternoon to fix my home connection.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO additional_wfh_info_table (request_id, wfh_commitment, work_location, total_days)
VALUES (
    'req-007',
    true,
    '456 Le Loi Street, District 3, Ho Chi Minh City',
    0.5
);

INSERT INTO wfh_dates_table (wfh_date_id, request_id, date, shift) VALUES
    ('wfh-004', 'req-007', CURRENT_DATE + CAST('1 days' AS INTERVAL), 'AFTERNOON');

-- Add comments about sample data
COMMENT ON TABLE request_table IS 'Sample data includes various request types and statuses for testing';
