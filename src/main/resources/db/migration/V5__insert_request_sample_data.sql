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
    CURRENT_TIMESTAMP - INTERVAL '2' HOUR,
    CURRENT_TIMESTAMP - INTERVAL '1' DAY,
    CURRENT_TIMESTAMP - INTERVAL '2' HOUR
);

INSERT INTO additional_checkin_info_table (request_id, desired_check_in_time, current_check_in_time)
VALUES (
    'req-001',
    CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '8' HOUR,
    CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '9' HOUR
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
    CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '17' HOUR,
    CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '15' HOUR
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
    CURRENT_TIMESTAMP - INTERVAL '1' HOUR,
    CURRENT_TIMESTAMP - INTERVAL '1' DAY,
    CURRENT_TIMESTAMP - INTERVAL '1' HOUR
);

INSERT INTO additional_timesheet_info_table (
    request_id, desired_check_in_time, current_check_in_time,
    desired_check_out_time, current_check_out_time, target_date
) VALUES (
    'req-003',
    CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '8' HOUR,
    CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '8' HOUR,
    CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '17' HOUR,
    CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '17' HOUR,
    CURRENT_DATE - INTERVAL '1' DAY
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
    ('ld-001', 'req-004', CURRENT_DATE + INTERVAL '7' DAY, 'FULL_DAY'),
    ('ld-002', 'req-004', CURRENT_DATE + INTERVAL '8' DAY, 'FULL_DAY'),
    ('ld-003', 'req-004', CURRENT_DATE + INTERVAL '9' DAY, 'MORNING');

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
    CURRENT_TIMESTAMP - INTERVAL '30' MINUTE,
    CURRENT_TIMESTAMP - INTERVAL '2' HOUR,
    CURRENT_TIMESTAMP - INTERVAL '30' MINUTE
);

INSERT INTO additional_leave_info_table (request_id, leave_type, total_days)
VALUES ('req-005', 'SICK', 2.0);

INSERT INTO leave_dates_table (leave_date_id, request_id, date, shift) VALUES
    ('ld-004', 'req-005', CURRENT_DATE, 'FULL_DAY'),
    ('ld-005', 'req-005', CURRENT_DATE + INTERVAL '1' DAY, 'FULL_DAY');

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
    CURRENT_TIMESTAMP - INTERVAL '3' HOUR,
    'https://storage.example.com/attachments/renovation-schedule.pdf',
    CURRENT_TIMESTAMP - INTERVAL '2' DAY,
    CURRENT_TIMESTAMP - INTERVAL '3' HOUR
);

INSERT INTO additional_wfh_info_table (request_id, wfh_commitment, work_location, total_days)
VALUES (
    'req-006',
    true,
    '123 Nguyen Hue Street, District 1, Ho Chi Minh City',
    3.0
);

INSERT INTO wfh_dates_table (wfh_date_id, request_id, date, shift) VALUES
    ('wfh-001', 'req-006', CURRENT_DATE + INTERVAL '3' DAY, 'FULL_DAY'),
    ('wfh-002', 'req-006', CURRENT_DATE + INTERVAL '4' DAY, 'FULL_DAY'),
    ('wfh-003', 'req-006', CURRENT_DATE + INTERVAL '5' DAY, 'FULL_DAY');

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
    ('wfh-004', 'req-007', CURRENT_DATE + INTERVAL '1' DAY, 'AFTERNOON');

-- Add comments about sample data
COMMENT ON TABLE request_table IS 'Sample data includes various request types and statuses for testing';
