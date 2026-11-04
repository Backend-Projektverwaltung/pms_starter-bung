package de.szut.pms.project;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.szut.pms.project.ProjectDtos.ProjectRequest;
import de.szut.pms.project.ProjectDtos.ProjectResponse;
import de.szut.pms.project.ProjectDtos.EmployeeAssignmentRequest;
import de.szut.pms.project.ProjectDtos.ProjectEmployeesResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@RequestBody @Valid ProjectRequest request) {
        ProjectResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id,
                                                  @RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/employees")
    public ResponseEntity<ProjectResponse> assignEmployee(@PathVariable Long projectId,
                                                           @RequestBody @Valid EmployeeAssignmentRequest request) {
        return ResponseEntity.ok(service.assignEmployee(projectId, request));
    }

    @GetMapping("/{projectId}/employees")
    public ResponseEntity<ProjectEmployeesResponse> findEmployees(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.findEmployees(projectId));
    }

    @DeleteMapping("/{projectId}/employees/{employeeId}")
    public ResponseEntity<Void> removeEmployee(@PathVariable Long projectId, @PathVariable Long employeeId) {
        service.removeEmployee(projectId, employeeId);
        return ResponseEntity.noContent().build();
    }
}
