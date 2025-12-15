package org.pqkkkkk.hr_management_server.modules.request.domain.event;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class RequestCreatedEvent extends ApplicationEvent {
    private final Request request;
    
    public RequestCreatedEvent(Object source, Request request) {
        super(source);
        this.request = request;
    }
}
