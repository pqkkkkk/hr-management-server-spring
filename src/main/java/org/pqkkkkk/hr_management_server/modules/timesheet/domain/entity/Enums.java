package org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity;

/**
 * Enums for Timesheet module
 */
public class Enums {
    /**
     * Attendance status for morning/afternoon shifts
     * - PRESENT: Employee is present (working normally or WFH)
     * - ABSENT: Employee is absent without approved leave
     * - LEAVE: Employee is on approved leave (not paid)
     */
    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        LEAVE
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
