-- Additional LEAVE request sample data for comprehensive integration testing

-- Sample LEAVE request (APPROVED) - For testing approve/reject on already processed request
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, 
    employee_id, approver_id, processor_id, processed_at, created_at, updated_at
) VALUES (
    'req-leave-approved', 'LEAVE', 'APPROVED',
    'Annual Leave - Beach Vacation (Already Approved)',
    'Family beach vacation - already approved by manager.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1),
    'u2b3c4d5-f6a7-8901-bcde-f12345678901',
    'u2b3c4d5-f6a7-8901-bcde-f12345678901',
    CURRENT_TIMESTAMP - INTERVAL '1' DAY,
    CURRENT_TIMESTAMP - INTERVAL '5' DAY,
    CURRENT_TIMESTAMP - INTERVAL '1' DAY
);

INSERT INTO additional_leave_info_table (request_id, leave_type, total_days)
VALUES ('req-leave-approved', 'ANNUAL', 3.0);

INSERT INTO leave_dates_table (leave_date_id, request_id, date, shift) VALUES
    ('ld-approved-001', 'req-leave-approved', CURRENT_DATE + INTERVAL '10' DAY, 'FULL_DAY'),
    ('ld-approved-002', 'req-leave-approved', CURRENT_DATE + INTERVAL '11' DAY, 'FULL_DAY'),
    ('ld-approved-003', 'req-leave-approved', CURRENT_DATE + INTERVAL '12' DAY, 'FULL_DAY');

-- PENDING request for approval testing (valid scenario)
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, 
    employee_id, approver_id, processor_id, created_at, updated_at
) VALUES (
    'req-leave-pending-valid', 'LEAVE', 'PENDING',
    'Annual Leave - Christmas Holiday',
    'Planning Christmas vacation with family.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1 OFFSET 1),
    'u2b3c4d5-f6a7-8901-bcde-f12345678901',
    'u2b3c4d5-f6a7-8901-bcde-f12345678901',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO additional_leave_info_table (request_id, leave_type, total_days)
VALUES ('req-leave-pending-valid', 'ANNUAL', 5.0);

INSERT INTO leave_dates_table (leave_date_id, request_id, date, shift) VALUES
    ('ld-pending-001', 'req-leave-pending-valid', CURRENT_DATE + INTERVAL '20' DAY, 'FULL_DAY'),
    ('ld-pending-002', 'req-leave-pending-valid', CURRENT_DATE + INTERVAL '21' DAY, 'FULL_DAY'),
    ('ld-pending-003', 'req-leave-pending-valid', CURRENT_DATE + INTERVAL '22' DAY, 'FULL_DAY'),
    ('ld-pending-004', 'req-leave-pending-valid', CURRENT_DATE + INTERVAL '23' DAY, 'FULL_DAY'),
    ('ld-pending-005', 'req-leave-pending-valid', CURRENT_DATE + INTERVAL '24' DAY, 'FULL_DAY');

-- PENDING request with different approver (for permission testing)
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, 
    employee_id, approver_id, processor_id, created_at, updated_at
) VALUES (
    'req-leave-different-approver', 'LEAVE', 'PENDING',
    'Annual Leave - Short Trip',
    'Quick weekend trip.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1 OFFSET 2),
    (SELECT user_id FROM user_table WHERE role = 'MANAGER' LIMIT 1 OFFSET 1),
    (SELECT user_id FROM user_table WHERE role = 'MANAGER' LIMIT 1 OFFSET 1),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO additional_leave_info_table (request_id, leave_type, total_days)
VALUES ('req-leave-different-approver', 'ANNUAL', 2.0);

INSERT INTO leave_dates_table (leave_date_id, request_id, date, shift) VALUES
    ('ld-diff-001', 'req-leave-different-approver', CURRENT_DATE + INTERVAL '14' DAY, 'FULL_DAY'),
    ('ld-diff-002', 'req-leave-different-approver', CURRENT_DATE + INTERVAL '15' DAY, 'FULL_DAY');

-- PENDING UNPAID leave request
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, 
    employee_id, approver_id, processor_id, created_at, updated_at
) VALUES (
    'req-leave-unpaid', 'LEAVE', 'PENDING',
    'Unpaid Leave - Personal Matters',
    'Need time off for urgent personal matters.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1),
    'u2b3c4d5-f6a7-8901-bcde-f12345678901',
    'u2b3c4d5-f6a7-8901-bcde-f12345678901',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO additional_leave_info_table (request_id, leave_type, total_days)
VALUES ('req-leave-unpaid', 'UNPAID', 1.5);

INSERT INTO leave_dates_table (leave_date_id, request_id, date, shift) VALUES
    ('ld-unpaid-001', 'req-leave-unpaid', CURRENT_DATE + INTERVAL '18' DAY, 'FULL_DAY'),
    ('ld-unpaid-002', 'req-leave-unpaid', CURRENT_DATE + INTERVAL '19' DAY, 'MORNING');

-- PENDING request with half days (for calculation testing)
INSERT INTO request_table (
    request_id, request_type, status, title, user_reason, 
    employee_id, approver_id, processor_id, created_at, updated_at
) VALUES (
    'req-leave-half-days', 'LEAVE', 'PENDING',
    'Annual Leave - Half Days',
    'Need afternoon off for appointments.',
    (SELECT user_id FROM user_table WHERE role = 'EMPLOYEE' LIMIT 1),
    'u2b3c4d5-f6a7-8901-bcde-f12345678901',
    'u2b3c4d5-f6a7-8901-bcde-f12345678901',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO additional_leave_info_table (request_id, leave_type, total_days)
VALUES ('req-leave-half-days', 'ANNUAL', 1.5);

INSERT INTO leave_dates_table (leave_date_id, request_id, date, shift) VALUES
    ('ld-half-001', 'req-leave-half-days', CURRENT_DATE + INTERVAL '25' DAY, 'MORNING'),
    ('ld-half-002', 'req-leave-half-days', CURRENT_DATE + INTERVAL '26' DAY, 'FULL_DAY');

-- Add comments
COMMENT ON TABLE request_table IS 'Extended sample data includes APPROVED, PENDING requests with various scenarios for comprehensive integration testing';
