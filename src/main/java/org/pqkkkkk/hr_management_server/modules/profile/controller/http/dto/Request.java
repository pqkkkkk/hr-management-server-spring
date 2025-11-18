package org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;

/**
 * Request DTOs for Profile module.
 * <p> They are used to encapsulate data sent by clients in HTTP requests.
 * <p> Each request DTO includes methods to convert to entity objects.
 */
public class Request {
    public record CreateStaffRequest(
            String fullName
    ){
        public User toEntity(){
            return null;
        }
    }
}
