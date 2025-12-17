-- Sample data for Request Delegation testing
-- This migration adds specific test data for delegation functionality testing

-- Add a PENDING request specifically for delegation testing
INSERT INTO request_table (
    request_id, request_type, status,
    title, user_reason, attachment_url,
    employee_id, approver_id, processor_id,
    processed_at, created_at, updated_at
) VALUES (
    'req-delegation-pending', 'CHECK_IN', 'PENDING',
    'Check-in request for delegation test',
    'Need to delegate this request',
    NULL,
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890', -- Employee
    'u2b3c4d5-f6a7-8901-bcde-f12345678901', -- Manager as default approver
    'u2b3c4d5-f6a7-8901-bcde-f12345678901', -- Manager as default processor
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '1' DAY,
    CURRENT_TIMESTAMP - INTERVAL '1' DAY
);

-- Add additional check-in info for this request
INSERT INTO additional_checkin_info_table (
    request_id,
    desired_check_in_time,
    current_check_in_time
) VALUES (
    'req-delegation-pending',
    CURRENT_TIMESTAMP + INTERVAL '1' HOUR,
    NULL
);

-- Add an APPROVED request to test that non-pending requests cannot be delegated
INSERT INTO request_table (
    request_id, request_type, status,
    title, user_reason, attachment_url,
    employee_id, approver_id, processor_id,
    processed_at, created_at, updated_at
) VALUES (
    'req-delegation-approved', 'CHECK_OUT', 'APPROVED',
    'Check-out request - already approved',
    'This request is already approved',
    NULL,
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890', -- Employee
    'u2b3c4d5-f6a7-8901-bcde-f12345678901', -- Manager
    'u2b3c4d5-f6a7-8901-bcde-f12345678901', -- Manager
    CURRENT_TIMESTAMP - INTERVAL '2' HOUR,
    CURRENT_TIMESTAMP - INTERVAL '1' DAY,
    CURRENT_TIMESTAMP - INTERVAL '2' HOUR
);

-- Add additional check-out info for the approved request
INSERT INTO additional_checkout_info_table (
    request_id,
    desired_check_out_time,
    current_check_out_time
) VALUES (
    'req-delegation-approved',
    CURRENT_TIMESTAMP - INTERVAL '3' HOUR,
    NULL
);

COMMENT ON TABLE request_table IS 'Added delegation test data: 1 PENDING request for successful delegation, 1 APPROVED request to test validation';
