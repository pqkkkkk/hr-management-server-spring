package org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity;

/**
 * Enums for Timesheet module
 */
public class Enums {
    public enum AttendanceStatus {
        WORKING,
        ABSENT,
        LEAVE_PAID,
        LEAVE_UNPAID,
        LATE,
        EARLY_LEAVE
    }
    public enum TimeSheetSortingField {
        DATE ("date");

        private final String fieldName;
        
        TimeSheetSortingField(String fieldName) {
            this.fieldName = fieldName;
        }
        public String getFieldName() {
            return fieldName;
        }

    }
}
