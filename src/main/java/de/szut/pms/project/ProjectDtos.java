package de.szut.pms.project;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ProjectDtos {
    private ProjectDtos() {}

    public record ProjectRequest(
            @NotBlank String name,
            @NotNull Long responsibleEmployeeId,
            @NotNull Long customerId,
            @NotBlank String customerContactName,
            String comment,
            @NotNull LocalDate startDate,
            LocalDate endDate) {
    }

    public record ProjectResponse(
            Long id,
            String name,
            Long responsibleEmployeeId,
            Long customerId,
            String customerContactName,
            String comment,
            LocalDate startDate,
            LocalDate endDate,
            List<EmployeeResponse> employees) {
    }

    public record EmployeeResponse(Long employeeId, String qualification) {
    }
}
