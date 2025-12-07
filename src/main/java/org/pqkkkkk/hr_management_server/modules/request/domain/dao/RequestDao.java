package org.pqkkkkk.hr_management_server.modules.request.domain.dao;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;

public interface RequestDao {
    public Request createRequest(Request request);
    public Request updateRequest(Request request);
    public Request getRequestById(String requestId);
}
