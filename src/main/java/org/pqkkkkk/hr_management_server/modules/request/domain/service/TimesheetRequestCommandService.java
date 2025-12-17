package org.pqkkkkk.hr_management_server.modules.request.domain.service;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;

public interface TimesheetRequestCommandService extends RequestCommandService {
    @Override
    Request createRequest(Request request);
}
