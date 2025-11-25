package org.pqkkkkk.hr_management_server.modules.profile.controller.http;

import jakarta.validation.Valid;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.DTO;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Request;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl.DeparmentServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
public class DepartmentApi {
    private final DeparmentServiceImpl deparmentService;

    public DepartmentApi(DeparmentServiceImpl deparmentService) {
        this.deparmentService = deparmentService;
    }

    @PostMapping
    public ResponseEntity<Response.ApiResponse<DTO.DepartmentDTO>> createDepartment(@Valid @RequestBody Request.DepartmentRequest request) {
        Department department = request.toEntity();
        Department createdDepartment = deparmentService.createDepartment(department);

        DTO.DepartmentDTO departmentDTO = DTO.DepartmentDTO.fromEntity(createdDepartment);

        Response.ApiResponse<DTO.DepartmentDTO> apiResponse = new Response.ApiResponse<>(
                departmentDTO,
                true,
                HttpStatus.CREATED.value(),
                "Department created successfully.",
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}
