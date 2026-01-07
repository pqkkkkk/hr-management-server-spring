-- V21: Add Comprehensive Notification Sample Data
-- Description: Create diverse notification data covering all types, roles, and statuses
-- Profile: dev, docker, gcp ONLY (not loaded in test profile)

-- Notifications for various request events (last 7 days)
INSERT INTO
    notification_table (
        notification_id,
        recipient_id,
        title,
        message,
        notification_type,
        reference_type,
        reference_id,
        is_read,
        created_at
    )
VALUES
    -- REQUEST_CREATED notifications (for HR/Manager - pending approvals)
    (
        'n001-0001-0001-0001-000000000001',
        'u2b3c4d5-f6a7-8901-bcde-f12345678901', -- Le Thi Binh (HR)
        'New Check-In Request',
        'Nguyen Van An submitted a late check-in request for 2024-12-02 at 09:00 AM.',
        'REQUEST_CREATED',
        'REQUEST',
        'r001-checkin-001',
        false,
        NOW() - INTERVAL '1 day'
    ),
    (
        'n001-0001-0001-0001-000000000002',
        'u4d5e6f7-b8c9-0123-def0-234567890123', -- Pham Van Dat (MANAGER)
        'New Leave Request',
        'Nguyen Van An submitted an annual leave request for 2024-12-05 (Morning).',
        'REQUEST_CREATED',
        'REQUEST',
        'r003-leave-001',
        false,
        NOW() - INTERVAL '2 days'
    ),
    (
        'n001-0001-0001-0001-000000000003',
        'u2b3c4d5-f6a7-8901-bcde-f12345678901', -- Le Thi Binh (HR)
        'New WFH Request',
        'Tran Thi Cuc submitted a work from home request for 2024-12-10 (Full Day).',
        'REQUEST_CREATED',
        'REQUEST',
        'r005-wfh-001',
        true,
        NOW() - INTERVAL '3 days'
    ),
    (
        'n001-0001-0001-0001-000000000004',
        'u4d5e6f7-b8c9-0123-def0-234567890123', -- Pham Van Dat (MANAGER)
        'New Timesheet Correction',
        'Hoang Van Em submitted a timesheet correction request for 2024-12-01.',
        'REQUEST_CREATED',
        'REQUEST',
        'r002-timesheet-001',
        false,
        NOW() - INTERVAL '4 days'
    ),
    (
        'n001-0001-0001-0001-000000000005',
        'u2b3c4d5-f6a7-8901-bcde-f12345678901', -- Le Thi Binh (HR)
        'New Check-Out Request',
        'Vo Thi Phuong submitted an early check-out request for 2024-12-03 at 04:00 PM.',
        'REQUEST_CREATED',
        'REQUEST',
        'r004-checkout-001',
        true,
        NOW() - INTERVAL '5 days'
    ),

-- REQUEST_APPROVED notifications (for employees)
(
    'n001-0001-0001-0001-000000000006',
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890', -- Nguyen Van An (EMPLOYEE)
    'Request Approved',
    'Your check-in request for 2024-12-02 has been approved by Le Thi Binh.',
    'REQUEST_APPROVED',
    'REQUEST',
    'r001-checkin-001',
    true,
    NOW() - INTERVAL '1 day'
),
(
    'n001-0001-0001-0001-000000000007',
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890', -- Nguyen Van An (EMPLOYEE)
    'Leave Approved',
    'Your annual leave request for 2024-12-05 has been approved by Pham Van Dat.',
    'REQUEST_APPROVED',
    'REQUEST',
    'r003-leave-001',
    false,
    NOW() - INTERVAL '2 days'
),
(
    'n001-0001-0001-0001-000000000008',
    'u3c4d5e6-a7b8-9012-cdef-123456789012', -- Tran Thi Cuc (EMPLOYEE)
    'WFH Request Approved',
    'Your work from home request for 2024-12-10 has been approved by Le Thi Binh.',
    'REQUEST_APPROVED',
    'REQUEST',
    'r005-wfh-001',
    true,
    NOW() - INTERVAL '3 days'
),
(
    'n001-0001-0001-0001-000000000009',
    'u5e6f7a8-c9d0-1234-ef01-345678901234', -- Hoang Van Em (EMPLOYEE)
    'Timesheet Correction Approved',
    'Your timesheet correction for 2024-12-01 has been approved by Pham Van Dat.',
    'REQUEST_APPROVED',
    'REQUEST',
    'r002-timesheet-001',
    false,
    NOW() - INTERVAL '4 days'
),
(
    'n001-0001-0001-0001-000000000010',
    'u6f7a8b9-d0e1-2345-f012-456789012345', -- Vo Thi Phuong (EMPLOYEE)
    'Early Check-Out Approved',
    'Your early check-out request for 2024-12-03 has been approved by Le Thi Binh.',
    'REQUEST_APPROVED',
    'REQUEST',
    'r004-checkout-001',
    true,
    NOW() - INTERVAL '5 days'
),

-- REQUEST_REJECTED notifications (for employees with rejection reasons)
(
    'n001-0001-0001-0001-000000000011',
    'u8b9c0d1-f2a3-4567-1234-678901234567', -- Nguyen Thi Hoa (EMPLOYEE)
    'Request Rejected',
    'Your sick leave request for 2024-12-08 has been rejected by Le Thi Binh. Reason: Please provide medical certificate for sick leave longer than 2 days.',
    'REQUEST_REJECTED',
    'REQUEST',
    'r003-leave-002',
    false,
    NOW() - INTERVAL '1 day'
),
(
    'n001-0001-0001-0001-000000000012',
    'u0d1e2f3-b4c5-6789-3456-890123456789', -- Le Thi Lan (EMPLOYEE)
    'WFH Request Rejected',
    'Your work from home request for 2024-12-15 has been rejected by Pham Van Dat. Reason: WFH quota exceeded for this month. Please check your remaining WFH days.',
    'REQUEST_REJECTED',
    'REQUEST',
    'r005-wfh-002',
    false,
    NOW() - INTERVAL '2 days'
),

-- Additional mixed read/unread notifications
(
    'n001-0001-0001-0001-000000000013',
    'u1a2b3c4-e5f6-7890-abcd-ef1234567890', -- Nguyen Van An (EMPLOYEE)
    'Timesheet Reminder',
    'Please review and finalize your timesheets for last month.',
    'SYSTEM',
    'SYSTEM',
    NULL,
    false,
    NOW() - INTERVAL '6 hours'
),
(
    'n001-0001-0001-0001-000000000014',
    'u4d5e6f7-b8c9-0123-def0-234567890123', -- Pham Van Dat (MANAGER)
    'Pending Approvals',
    'You have 3 pending requests awaiting your approval.',
    'SYSTEM',
    'SYSTEM',
    NULL,
    false,
    NOW() - INTERVAL '12 hours'
),
(
    'n001-0001-0001-0001-000000000015',
    'u2b3c4d5-f6a7-8901-bcde-f12345678901', -- Le Thi Binh (HR)
    'Monthly Report Due',
    'The monthly attendance report is due in 3 days.',
    'SYSTEM',
    'SYSTEM',
    NULL,
    true,
    NOW() - INTERVAL '18 hours'
),
(
    'n001-0001-0001-0001-000000000016',
    'u7a8b9c0-e1f2-3456-0123-567890123456', -- Nguyen Van Giang (ADMIN)
    'System Maintenance',
    'Scheduled system maintenance on Saturday 2024-12-14 from 2:00 AM to 4:00 AM.',
    'SYSTEM',
    'SYSTEM',
    NULL,
    true,
    NOW() - INTERVAL '1 day'
),
(
    'n001-0001-0001-0001-000000000017',
    'u3c4d5e6-a7b8-9012-cdef-123456789012', -- Tran Thi Cuc (EMPLOYEE)
    'Leave Balance Update',
    'Your annual leave balance has been updated. You have 8 days remaining.',
    'SYSTEM',
    'SYSTEM',
    NULL,
    false,
    NOW() - INTERVAL '2 days'
),
(
    'n001-0001-0001-0001-000000000018',
    'u5e6f7a8-c9d0-1234-ef01-345678901234', -- Hoang Van Em (EMPLOYEE)
    'Welcome Message',
    'Welcome to the HR Management System! Please complete your profile information.',
    'SYSTEM',
    'SYSTEM',
    NULL,
    true,
    NOW() - INTERVAL '7 days'
);

-- Verify: Should have ~18 notifications total covering various scenarios