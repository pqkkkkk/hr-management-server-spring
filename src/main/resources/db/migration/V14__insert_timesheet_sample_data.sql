-- Insert sample timesheet data for testing

-- Sample data for employee: u1a2b3c4-e5f6-7890-abcd-ef1234567890 (Nguyen Van An)
-- Date: 2024-12-01 (past date, for testing existing timesheet scenarios)
INSERT INTO daily_timesheet_table (
    daily_ts_id,
    date,
    morning_status,
    afternoon_status,
    morning_wfh,
    afternoon_wfh,
    total_work_credit,
    check_in_time,
    check_out_time,
    late_minutes,
    early_leave_minutes,
    overtime_minutes,
    is_finalized,
    employee_id
) VALUES
-- Normal working day - on time check-in and check-out
(
    'ts1a2b3c-d4e5-6789-abcd-ef1234567890',
    '2024-12-01',
    'PRESENT',
    'PRESENT',
    false,
    false,
    1.0,
    '2024-12-01 08:00:00',
    '2024-12-01 17:00:00',
    0,
    0,
    0,
    false,
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890'
),
-- Late check-in, on-time check-out
(
    'ts2b3c4d-e5f6-7890-bcde-f12345678901',
    '2024-12-02',
    'PRESENT',
    'PRESENT',
    false,
    false,
    0.889,
    '2024-12-02 09:00:00',
    '2024-12-02 17:00:00',
    60,
    0,
    0,
    false,
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890'
),
-- Full day leave
(
    'ts3c4d5e-f6a7-8901-cdef-123456789012',
    '2024-12-03',
    'LEAVE',
    'LEAVE',
    false,
    false,
    0.0,
    null,
    null,
    0,
    0,
    0,
    false,
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890'
),
-- Full day WFH
(
    'ts4d5e6f-a7b8-9012-def0-234567890123',
    '2024-12-04',
    'PRESENT',
    'PRESENT',
    true,
    true,
    1.0,
    null,
    null,
    0,
    0,
    0,
    false,
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890'
),
-- Finalized timesheet (for testing cannot update finalized)
(
    'ts5e6f7a-b8c9-0123-ef01-345678901234',
    '2024-12-05',
    'PRESENT',
    'PRESENT',
    false,
    false,
    1.0,
    '2024-12-05 08:00:00',
    '2024-12-05 17:00:00',
    0,
    0,
    0,
    true,
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890'
);

-- Sample data for another employee: u2b3c4d5-f6a7-8901-bcde-f12345678901
-- One timesheet for testing
INSERT INTO daily_timesheet_table (
    daily_ts_id,
    date,
    morning_status,
    afternoon_status,
    morning_wfh,
    afternoon_wfh,
    total_work_credit,
    check_in_time,
    check_out_time,
    late_minutes,
    early_leave_minutes,
    overtime_minutes,
    is_finalized,
    employee_id
) VALUES
(
    'ts6f7a8b-c9d0-1234-f012-456789012345',
    '2024-12-01',
    'PRESENT',
    'PRESENT',
    false,
    false,
    1.0,
    '2024-12-01 08:00:00',
    '2024-12-01 17:00:00',
    0,
    0,
    0,
    false,
    'u2b3c4d5-f6a7-8901-bcde-f12345678901'
);
