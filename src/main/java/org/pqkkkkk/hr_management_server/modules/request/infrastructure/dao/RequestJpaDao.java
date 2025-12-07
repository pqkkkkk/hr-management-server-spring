package org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository.RequestRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RequestJpaDao implements RequestDao {
    
    private final RequestRepository requestRepository;

    public RequestJpaDao(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public Request createRequest(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        // Ensure this is a new request (no ID set yet, or ID doesn't exist)
        if (request.getRequestId() != null && requestRepository.existsById(request.getRequestId())) {
            throw new IllegalArgumentException("Request with ID " + request.getRequestId() + " already exists");
        }
        
        return requestRepository.save(request);
    }

    @Override
    public Request updateRequest(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getRequestId() == null) {
            throw new IllegalArgumentException("Request ID cannot be null for update operation");
        }
        
        return requestRepository.save(request);
    }

    @Override
    public Request getRequestById(String requestId) {
        return requestRepository.findById(requestId).orElse(null);
    }
}
