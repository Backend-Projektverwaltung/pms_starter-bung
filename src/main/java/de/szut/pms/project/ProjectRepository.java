package de.szut.pms.project;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> findByEmployeesEmployeeId(Long employeeId);
}
