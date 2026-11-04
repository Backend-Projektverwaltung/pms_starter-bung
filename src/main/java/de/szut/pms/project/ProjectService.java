package de.szut.pms.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeClient.EmployeeResponse;
import de.szut.pms.employee.EmployeeClient.SkillResponse;
import de.szut.pms.project.ProjectDtos.ProjectRequest;
import de.szut.pms.project.ProjectDtos.ProjectResponse;
import de.szut.pms.project.ProjectDtos.EmployeeAssignmentRequest;
import de.szut.pms.project.ProjectDtos.ProjectEmployeesResponse;
import de.szut.pms.project.ProjectDtos.EmployeeProjectResponse;
import de.szut.pms.customer.CustomerClient;
import de.szut.pms.customer.CustomerNotFoundException;
import de.szut.pms.project.ProjectDtos.EmployeeProjectsResponse;

@Service
public class ProjectService {

    private final ProjectRepository repository;
    private final EmployeeClient employeeClient;
    private final CustomerClient customerClient;

    public ProjectService(
            ProjectRepository repository,
            EmployeeClient employeeClient,
            CustomerClient customerClient) {

        this.repository = repository;
        this.employeeClient = employeeClient;
        this.customerClient = customerClient;
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        validateDates(request);
        validateCustomer(request);
        validateResponsibleEmployee(request);

        ProjectEntity entity = new ProjectEntity();
        copy(request, entity);

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        ProjectEntity entity = findEntity(id);

        validateDates(request);
        validateResponsibleEmployee(request);
        validateCustomer(request);

        copy(request, entity);

        return toResponse(repository.save(entity));
    }

    @Transactional
    public void deleteById(Long id) {
        ProjectEntity entity = findEntity(id);
        repository.delete(entity);
    }

    @Transactional
    public ProjectResponse assignEmployee(Long projectId, EmployeeAssignmentRequest request) {
        ProjectEntity project = findEntity(projectId);
        EmployeeResponse validatedEmployee = employeeClient.findById(request.employeeId());
        List<SkillResponse> skills = validatedEmployee.skillSet() == null
                ? List.of()
                : validatedEmployee.skillSet();
        SkillResponse qualification = skills.stream()
                .filter(skill -> skill != null)
                .filter(skill -> skill.skill() != null
                        && skill.skill().equalsIgnoreCase(request.qualification().trim()))
                .findFirst()
                .orElseThrow(() -> new EmployeeQualificationNotFoundException(
                        request.qualification(), request.employeeId()));

        boolean alreadyAssigned = project.getEmployees().stream()
                .anyMatch(employee -> employee.getEmployeeId().equals(request.employeeId()));
        if (alreadyAssigned) {
            throw new ProjectAssignmentConflictException(
                    "Der Mitarbeiter ist dem Projekt bereits zugeordnet.");
        }

        boolean overlaps = repository.findByEmployeesEmployeeId(request.employeeId()).stream()
                .filter(other -> !other.getId().equals(projectId))
                .anyMatch(other -> periodsOverlap(project, other));
        if (overlaps) {
            throw new ProjectAssignmentConflictException(
                    "Der Mitarbeiter ist im Projektzeitraum bereits einem anderen Projekt zugeordnet.");
        }

        ProjectEmployeeEntity assignment = new ProjectEmployeeEntity();
        assignment.setProject(project);
        assignment.setEmployeeId(request.employeeId());
        assignment.setQualification(qualification.skill());
        project.getEmployees().add(assignment);
        return toResponse(repository.save(project));
    }

    @Transactional(readOnly = true)
    public ProjectEmployeesResponse findEmployees(Long projectId) {
        ProjectEntity project = findEntity(projectId);
        return new ProjectEmployeesResponse(project.getId(), project.getName(), project.getEmployees().stream()
                .map(employee -> new ProjectDtos.EmployeeResponse(employee.getEmployeeId(), employee.getQualification()))
                .toList());
    }

    @Transactional(readOnly = true)
    public EmployeeProjectsResponse findProjectsByEmployeeId(Long employeeId) {

        employeeClient.findById(employeeId);

        List<EmployeeProjectResponse> projects =
                repository.findByEmployeesEmployeeId(employeeId).stream()
                        .map(project -> new EmployeeProjectResponse(
                                project.getId(),
                                project.getName(),
                                project.getStartDate(),
                                project.getEndDate(),
                                project.getEmployees().stream()
                                        .filter(employee ->
                                                employee.getEmployeeId().equals(employeeId))
                                        .findFirst()
                                        .orElseThrow(() ->
                                                new IllegalStateException(
                                                        "Die Projektzuordnung ist inkonsistent."))
                                        .getQualification()))
                        .toList();

        return new EmployeeProjectsResponse(employeeId, projects);
    }

    @Transactional
    public void removeEmployee(Long projectId, Long employeeId) {
        ProjectEntity project = findEntity(projectId);
        boolean removed = project.getEmployees().removeIf(employee -> employee.getEmployeeId().equals(employeeId));
        if (!removed) {
            throw new ProjectEmployeeNotFoundException(employeeId, projectId);
        }
        repository.save(project);
    }

    private boolean periodsOverlap(ProjectEntity first, ProjectEntity second) {
        boolean firstEndsAfterSecondStarts = first.getEndDate() == null
                || !first.getEndDate().isBefore(second.getStartDate());
        boolean secondEndsAfterFirstStarts = second.getEndDate() == null
                || !second.getEndDate().isBefore(first.getStartDate());
        return firstEndsAfterSecondStarts && secondEndsAfterFirstStarts;
    }

    private ProjectEntity findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    private void validateResponsibleEmployee(ProjectRequest request) {
        employeeClient.findById(request.responsibleEmployeeId());
    }

    private void validateCustomer(ProjectRequest request) {
        if (!customerClient.existsById(request.customerId())) {
            throw new CustomerNotFoundException(request.customerId());
        }
    }

    private void validateDates(ProjectRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Das Enddatum darf nicht vor dem Startdatum liegen.");
        }
    }

    private void copy(ProjectRequest request, ProjectEntity entity) {
        entity.setName(request.name());
        entity.setResponsibleEmployeeId(request.responsibleEmployeeId());
        entity.setCustomerId(request.customerId());
        entity.setCustomerContactName(request.customerContactName());
        entity.setComment(request.comment());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
    }

    private ProjectResponse toResponse(ProjectEntity entity) {
        return new ProjectResponse(entity.getId(), entity.getName(), entity.getResponsibleEmployeeId(),
                entity.getCustomerId(), entity.getCustomerContactName(), entity.getComment(), entity.getStartDate(),
                entity.getEndDate(), entity.getEmployees().stream()
                .map(employee -> new ProjectDtos.EmployeeResponse(employee.getEmployeeId(), employee.getQualification()))
                .toList());
    }
}
