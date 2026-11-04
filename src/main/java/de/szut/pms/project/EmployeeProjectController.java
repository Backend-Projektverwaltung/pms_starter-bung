package de.szut.pms.project;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.szut.pms.project.ProjectDtos.EmployeeProjectResponse;

@RestController
@RequestMapping("/employees/{employeeId}/projects")
public class EmployeeProjectController {

    private final ProjectService service;

    public EmployeeProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeProjectResponse>> findProjects(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.findProjectsByEmployeeId(employeeId));
    }
}
