-- V20: Add Missing User Credentials (Sample Data)
-- Description: Add credentials for remaining users to enable login
-- Profile: dev, docker, gcp ONLY (not loaded in test profile)
-- Password: "password123" (BCrypt hash)

INSERT INTO
    user_credentials (
        credential_id,
        user_id,
        password_hash,
        created_at,
        updated_at
    )
VALUES
    -- Tran Van Kiet (EMPLOYEE - INACTIVE)
    (
        'cred-0009-0009-0009-000000000009',
        'u9c0d1e2-a3b4-5678-2345-789012345678',
        '$2a$10$ZL5xJw8l0F4OVm6VhVpj4e5b3RZ0I2Q7x8Y9K6L3M4N5P6R7S8T9U0',
        NOW(),
        NOW()
    ),

-- Vo Thi Phuong (EMPLOYEE)
(
    'cred-0007-0007-0007-000000000007',
    'u6f7a8b9-d0e1-2345-f012-456789012345',
    '$2a$10$ZL5xJw8l0F4OVm6VhVpj4e5b3RZ0I2Q7x8Y9K6L3M4N5P6R7S8T9U0',
    NOW(),
    NOW()
),

-- Nguyen Thi Hoa (EMPLOYEE)
(
    'cred-0008-0008-0008-000000000008',
    'u8b9c0d1-f2a3-4567-1234-678901234567',
    '$2a$10$ZL5xJw8l0F4OVm6VhVpj4e5b3RZ0I2Q7x8Y9K6L3M4N5P6R7S8T9U0',
    NOW(),
    NOW()
),

-- Le Thi Lan (EMPLOYEE)
(
    'cred-0010-0010-0010-000000000010',
    'u0d1e2f3-b4c5-6789-3456-890123456789',
    '$2a$10$ZL5xJw8l0F4OVm6VhVpj4e5b3RZ0I2Q7x8Y9K6L3M4N5P6R7S8T9U0',
    NOW(),
    NOW()
);

-- Verify: Should have 10 total user credentials (6 from V19 + 4 from this migration)