package org.pqkkkkk.hr_management_server.modules.profile.domain.entity;

public class Enums {
    public enum UserRole {
        ADMIN,
        MANAGER,
        EMPLOYEE,
        HR
    }
    public enum UserGender {
        MALE,
        FEMALE,
        OTHER
    }
    public enum UserStatus {
        ACTIVE,
        INACTIVE
    }
    public enum UserPosition {
        INTERN,
        JUNIOR_DEVELOPER,
        SENIOR_DEVELOPER,
        TEAM_LEAD,
        PROJECT_MANAGER,
        HR_SPECIALIST,
        SALES_REPRESENTATIVE,
        MARKETING_MANAGER,
        CUSTOMER_SUPPORT,
        FINANCE_ANALYST,
        OPERATIONS_MANAGER
    }
    public enum UserSortingField {
        NAME("fullName"),
        ROLE("role");

        String fieldName;

        UserSortingField(String fieldName) {
            this.fieldName = fieldName;
        }
        public String getFieldName() {
            return fieldName;
        }
    }
}
