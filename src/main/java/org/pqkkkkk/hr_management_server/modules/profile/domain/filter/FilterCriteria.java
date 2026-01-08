package org.pqkkkkk.hr_management_server.modules.profile.domain.filter;

import java.util.List;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserSortingField;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserStatus;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserGender;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserPosition;

public class FilterCriteria {

    public record ProfileFilter(
            Integer currentPage,
            Integer pageSize,
            UserSortingField sortBy,
            SortDirection sortDirection,
            String nameTerm,
            List<UserRole> roles,
            UserGender gender,
            UserStatus status,
            UserPosition position,
            String departmentId,
            String departmentName) {

    }
}
