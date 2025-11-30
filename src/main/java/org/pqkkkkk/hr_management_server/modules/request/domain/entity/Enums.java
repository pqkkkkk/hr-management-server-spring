package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

public class Enums {
    
    public enum RequestType {
        CHECK_IN,
        CHECK_OUT,
        TIMESHEET,
        LEAVE,
        WFH
    }
    
    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED,
        PROCESSING
    }
    
    public enum LeaveType {
        ANNUAL,
        SICK,
        UNPAID,
        MATERNITY,
        PATERNITY,
        BEREAVEMENT,
        MARRIAGE,
        OTHER
    }
    
    public enum ShiftType {
        FULL_DAY,
        MORNING,
        AFTERNOON
    }
}
