-- V23: Add Timesheet Sample Data for Hoang Van Em - December 2025
-- Description: Create timesheet records for user Hoang Van Em (u5e6f7a8-c9d0-1234-ef01-345678901234)
-- Coverage: All 31 days in December 2025 with various realistic scenarios
-- Profile: dev, docker, gcp ONLY (not loaded in test profile)

-- User: Hoang Van Em (u5e6f7a8-c9d0-1234-ef01-345678901234)
-- December 2025 Calendar:
--   Week 1: Mon 1, Tue 2, Wed 3, Thu 4, Fri 5, Sat 6, Sun 7
--   Week 2: Mon 8, Tue 9, Wed 10, Thu 11, Fri 12, Sat 13, Sun 14
--   Week 3: Mon 15, Tue 16, Wed 17, Thu 18, Fri 19, Sat 20, Sun 21
--   Week 4: Mon 22, Tue 23, Wed 24, Thu 25, Fri 26, Sat 27, Sun 28
--   Week 5: Mon 29, Tue 30, Wed 31

INSERT INTO
    daily_timesheet_table (
        daily_ts_id,
        employee_id,
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
        is_finalized
    )
VALUES
    -- Day 1 (Mon, Dec 1, 2025): Normal working day
    (
        'ts-u5-2025-12-01',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-01',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-01 08:00:00',
        '2025-12-01 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 2 (Tue, Dec 2, 2025): Late arrival
    (
        'ts-u5-2025-12-02',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-02',
        'PRESENT',
        'PRESENT',
        false,
        false,
        0.944,
        '2025-12-02 08:30:00',
        '2025-12-02 17:00:00',
        30,
        0,
        0,
        true
    ),
    -- Day 3 (Wed, Dec 3, 2025): Normal working day
    (
        'ts-u5-2025-12-03',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-03',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-03 08:00:00',
        '2025-12-03 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 4 (Thu, Dec 4, 2025): Work from home
    (
        'ts-u5-2025-12-04',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-04',
        'PRESENT',
        'PRESENT',
        true,
        true,
        1.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 5 (Fri, Dec 5, 2025): Normal working day with overtime
    (
        'ts-u5-2025-12-05',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-05',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.111,
        '2025-12-05 08:00:00',
        '2025-12-05 18:00:00',
        0,
        0,
        60,
        true
    ),
    -- Day 6 (Sat, Dec 6, 2025): Weekend - no work
    (
        'ts-u5-2025-12-06',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-06',
        'ABSENT',
        'ABSENT',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 7 (Sun, Dec 7, 2025): Weekend - no work
    (
        'ts-u5-2025-12-07',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-07',
        'ABSENT',
        'ABSENT',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 8 (Mon, Dec 8, 2025): Normal working day
    (
        'ts-u5-2025-12-08',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-08',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-08 08:00:00',
        '2025-12-08 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 9 (Tue, Dec 9, 2025): Early leave
    (
        'ts-u5-2025-12-09',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-09',
        'PRESENT',
        'PRESENT',
        false,
        false,
        0.889,
        '2025-12-09 08:00:00',
        '2025-12-09 16:00:00',
        0,
        60,
        0,
        true
    ),
    -- Day 10 (Wed, Dec 10, 2025): Work from home morning, office afternoon
    (
        'ts-u5-2025-12-10',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-10',
        'PRESENT',
        'PRESENT',
        true,
        false,
        1.0,
        '2025-12-10 13:00:00',
        '2025-12-10 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 11 (Thu, Dec 11, 2025): Normal working day
    (
        'ts-u5-2025-12-11',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-11',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-11 08:00:00',
        '2025-12-11 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 12 (Fri, Dec 12, 2025): Normal working day
    (
        'ts-u5-2025-12-12',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-12',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-12 08:00:00',
        '2025-12-12 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 13 (Sat, Dec 13, 2025): Weekend - no work
    (
        'ts-u5-2025-12-13',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-13',
        'ABSENT',
        'ABSENT',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 14 (Sun, Dec 14, 2025): Weekend - no work
    (
        'ts-u5-2025-12-14',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-14',
        'ABSENT',
        'ABSENT',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 15 (Mon, Dec 15, 2025): Normal working day
    (
        'ts-u5-2025-12-15',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-15',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-15 08:00:00',
        '2025-12-15 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 16 (Tue, Dec 16, 2025): Late arrival with overtime to compensate
    (
        'ts-u5-2025-12-16',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-16',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-16 09:00:00',
        '2025-12-16 18:00:00',
        60,
        0,
        60,
        true
    ),
    -- Day 17 (Wed, Dec 17, 2025): Normal working day
    (
        'ts-u5-2025-12-17',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-17',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-17 08:00:00',
        '2025-12-17 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 18 (Thu, Dec 18, 2025): Annual leave
    (
        'ts-u5-2025-12-18',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-18',
        'LEAVE',
        'LEAVE',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 19 (Fri, Dec 19, 2025): Normal working day
    (
        'ts-u5-2025-12-19',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-19',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-19 08:00:00',
        '2025-12-19 17:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 20 (Sat, Dec 20, 2025): Weekend - no work
    (
        'ts-u5-2025-12-20',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-20',
        'ABSENT',
        'ABSENT',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 21 (Sun, Dec 21, 2025): Weekend - no work
    (
        'ts-u5-2025-12-21',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-21',
        'ABSENT',
        'ABSENT',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 22 (Mon, Dec 22, 2025): Normal working day with significant overtime
    (
        'ts-u5-2025-12-22',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-22',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.222,
        '2025-12-22 08:00:00',
        '2025-12-22 19:00:00',
        0,
        0,
        120,
        true
    ),
    -- Day 23 (Tue, Dec 23, 2025): Work from home
    (
        'ts-u5-2025-12-23',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-23',
        'PRESENT',
        'PRESENT',
        true,
        true,
        1.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 24 (Wed, Dec 24, 2025): Half day morning only - Christmas Eve
    (
        'ts-u5-2025-12-24',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-24',
        'PRESENT',
        'LEAVE',
        false,
        false,
        0.5,
        '2025-12-24 08:00:00',
        '2025-12-24 12:00:00',
        0,
        0,
        0,
        true
    ),
    -- Day 25 (Thu, Dec 25, 2025): Christmas Day - Public Holiday
    (
        'ts-u5-2025-12-25',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-25',
        'LEAVE',
        'LEAVE',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 26 (Fri, Dec 26, 2025): Annual leave
    (
        'ts-u5-2025-12-26',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-26',
        'LEAVE',
        'LEAVE',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 27 (Sat, Dec 27, 2025): Weekend - no work
    (
        'ts-u5-2025-12-27',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-27',
        'ABSENT',
        'ABSENT',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 28 (Sun, Dec 28, 2025): Weekend - no work
    (
        'ts-u5-2025-12-28',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-28',
        'ABSENT',
        'ABSENT',
        false,
        false,
        0.0,
        NULL,
        NULL,
        0,
        0,
        0,
        true
    ),
    -- Day 29 (Mon, Dec 29, 2025): Normal working day
    (
        'ts-u5-2025-12-29',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-29',
        'PRESENT',
        'PRESENT',
        false,
        false,
        1.0,
        '2025-12-29 08:00:00',
        '2025-12-29 17:00:00',
        0,
        0,
        0,
        false
    ),
    -- Day 30 (Tue, Dec 30, 2025): Normal working day with slight late
    (
        'ts-u5-2025-12-30',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-30',
        'PRESENT',
        'PRESENT',
        false,
        false,
        0.972,
        '2025-12-30 08:15:00',
        '2025-12-30 17:00:00',
        15,
        0,
        0,
        false
    ),
    -- Day 31 (Wed, Dec 31, 2025): New Year's Eve - Half day
    (
        'ts-u5-2025-12-31',
        'u5e6f7a8-c9d0-1234-ef01-345678901234',
        '2025-12-31',
        'PRESENT',
        'LEAVE',
        false,
        false,
        0.5,
        '2025-12-31 08:00:00',
        '2025-12-31 12:00:00',
        0,
        0,
        0,
        false
    )
ON CONFLICT (employee_id, date) DO NOTHING;