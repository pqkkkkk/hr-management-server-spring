package org.pqkkkkk.hr_management_server.modules.request.domain.service;

import org.pqkkkkk.hr_management_server.modules.request.domain.command.CreateLeaveRequestCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;

public interface LeaveRequestCommandService {
    Request createLeaveRequest(CreateLeaveRequestCommand command);
}
