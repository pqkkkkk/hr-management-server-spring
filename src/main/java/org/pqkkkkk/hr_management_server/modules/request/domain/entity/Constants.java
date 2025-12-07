package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

import java.math.BigDecimal;

public class Constants {
    // Just temporary constants for demonstration, will be saved in DB in future
    public static final BigDecimal MAX_LEAVE_BALANCE = BigDecimal.valueOf(30.0);
    
    // Just a placeholder remaining leave balance for demonstration, will be dynamically calculated in future
    public static final BigDecimal REMAINING_LEAVE_BALANCE = BigDecimal.valueOf(15.0);

    // Just a placeholder manager ID for demonstration, will be dynamically assigned in future
    public static final String DEFAULT_MANAGER_ID = "u2b3c4d5-f6a7-8901-bcde-f12345678901";
    
    // Minimum advance notice in working days (excludes weekends)
    public static final int MINIMUM_ADVANCE_NOTICE_DAYS = 2;
}
