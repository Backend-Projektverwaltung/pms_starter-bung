package de.szut.pms.employee;

import java.util.List;

public interface EmployeeClient {

    EmployeeResponse findById(Long employeeId);

    record EmployeeResponse(
            Long id,
            String firstName,
            String lastName,
            List<SkillResponse> skillSet
    ) {
    }

    record SkillResponse(
            String skill,
            Long id
    ) {
    }
}