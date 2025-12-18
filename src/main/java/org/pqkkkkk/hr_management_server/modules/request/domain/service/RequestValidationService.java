package org.pqkkkkk.hr_management_server.modules.request.domain.service;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;

public interface RequestValidationService {
    public Request checkRequestIsValid(String requestId);
    public void checkApproverPermissions(String approverId, Request request);
}
