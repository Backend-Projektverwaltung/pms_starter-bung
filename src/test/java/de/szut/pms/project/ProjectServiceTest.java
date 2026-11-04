package de.szut.pms.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.project.ProjectDtos.EmployeeAssignmentRequest;
import de.szut.pms.project.ProjectDtos.EmployeeProjectResponse;
import de.szut.pms.project.ProjectDtos.ProjectResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProjectServiceTest {

    private final ProjectRepository repository = mock(ProjectRepository.class);
    private final EmployeeClient employeeClient = mock(EmployeeClient.class);
    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(repository, employeeClient);
    }

    @Test
    void assignsEmployeeOnlyWithAnExistingQualification() {
        ProjectEntity project = project(10L, "Project A", LocalDate.of(2026, 1, 1), null);
        when(repository.findById(10L)).thenReturn(Optional.of(project));
        when(employeeClient.findById(4L)).thenReturn(employee(4L, "Java"));
        when(repository.findByEmployeesEmployeeId(4L)).thenReturn(List.of());
        when(repository.save(project)).thenReturn(project);

        ProjectResponse response = service.assignEmployee(
                10L, new EmployeeAssignmentRequest(4L, " java "));

        assertEquals("Java", response.employees().getFirst().qualification());
    }

    @Test
    void rejectsAssignmentWhenEmployeeDoesNotHaveTheQualification() {
        when(repository.findById(10L)).thenReturn(Optional.of(project(
                10L, "Project A", LocalDate.of(2026, 1, 1), null)));
        when(employeeClient.findById(4L)).thenReturn(employee(4L, "Java"));

        assertThrows(EmployeeQualificationNotFoundException.class, () -> service.assignEmployee(
                10L, new EmployeeAssignmentRequest(4L, "Kotlin")));
    }

    @Test
    void rejectsAssignmentWhenEmployeeIsAlreadyAssigned() {
        ProjectEntity project = project(10L, "Project A", LocalDate.of(2026, 1, 1), null);
        ProjectEmployeeEntity assignment = new ProjectEmployeeEntity();
        assignment.setEmployeeId(4L);
        project.getEmployees().add(assignment);
        when(repository.findById(10L)).thenReturn(Optional.of(project));
        when(employeeClient.findById(4L)).thenReturn(employee(4L, "Java"));

        assertThrows(ProjectAssignmentConflictException.class, () -> service.assignEmployee(
                10L, new EmployeeAssignmentRequest(4L, "Java")));
    }

    @Test
    void rejectsAssignmentWhenEmployeeProjectsOverlap() {
        ProjectEntity project = project(10L, "Project A", LocalDate.of(2026, 1, 1), null);
        ProjectEntity overlappingProject = project(
                11L, "Project B", LocalDate.of(2027, 1, 1), null);
        when(repository.findById(10L)).thenReturn(Optional.of(project));
        when(employeeClient.findById(4L)).thenReturn(employee(4L, "Java"));
        when(repository.findByEmployeesEmployeeId(4L)).thenReturn(List.of(overlappingProject));

        assertThrows(ProjectAssignmentConflictException.class, () -> service.assignEmployee(
                10L, new EmployeeAssignmentRequest(4L, "Java")));
    }

    @Test
    void returnsAnEmptyProjectListForAnExistingEmployeeWithoutAssignments() {
        when(employeeClient.findById(4L)).thenReturn(employee(4L, "Java"));
        when(repository.findByEmployeesEmployeeId(4L)).thenReturn(List.of());

        List<EmployeeProjectResponse> projects = service.findProjectsByEmployeeId(4L);

        assertEquals(List.of(), projects);
    }

    @Test
    void checksEmployeeExistenceBeforeReturningProjects() {
        when(employeeClient.findById(4L)).thenThrow(new EmployeeNotFoundException(4L));

        assertThrows(EmployeeNotFoundException.class, () -> service.findProjectsByEmployeeId(4L));
        verifyNoInteractions(repository);
    }

    @Test
    void returnsNotFoundWhenRemovingAnUnassignedEmployee() {
        when(repository.findById(10L)).thenReturn(Optional.of(
                project(10L, "Project A", LocalDate.of(2026, 1, 1), null)));

        assertThrows(ProjectEmployeeNotFoundException.class, () -> service.removeEmployee(10L, 4L));
        verify(repository).findById(10L);
    }

    private ProjectEntity project(Long id, String name, LocalDate startDate, LocalDate endDate) {
        ProjectEntity project = new ProjectEntity();
        project.setId(id);
        project.setName(name);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        return project;
    }

    private EmployeeClient.EmployeeResponse employee(Long id, String skill) {
        return new EmployeeClient.EmployeeResponse(
                id, "Test", "Person", List.of(new EmployeeClient.SkillResponse(skill, 1L)));
    }
}
