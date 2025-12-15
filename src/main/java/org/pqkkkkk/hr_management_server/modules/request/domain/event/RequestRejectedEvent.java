package org.pqkkkkk.hr_management_server.modules.request.domain.event;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class RequestRejectedEvent extends ApplicationEvent {
    private final Request request;
    
    public RequestRejectedEvent(Object source, Request request) {
        super(source);
        this.request = request;
    }
}
