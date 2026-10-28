package de.szut.pms.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.szut.pms.project.ProjectDtos.ProjectRequest;
import de.szut.pms.project.ProjectDtos.ProjectResponse;

@Service
public class ProjectService {

    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        validateDates(request);
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
        validateDates(request);
        ProjectEntity entity = findEntity(id);
        copy(request, entity);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void deleteById(Long id) {
        ProjectEntity entity = findEntity(id);
        repository.delete(entity);
    }

    private ProjectEntity findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
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