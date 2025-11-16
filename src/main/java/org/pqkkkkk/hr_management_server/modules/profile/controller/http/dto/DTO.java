package org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Staff;

/**
 * Data Transfer Objects for Profile module.
 * <p> Each DTO includes methods to convert to and from entity objects
 */
public class DTO {
    public record ProfileDTO(

    ){
        public Staff toEntity(){
            return Staff.builder()
                    .build();
        }
        public static ProfileDTO fromEntity(Staff staff){
            return new ProfileDTO(
            );
        }
    }
}
