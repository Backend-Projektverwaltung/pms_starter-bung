package de.szut.pms.project;

public class ProjectEmployeeNotFoundException extends RuntimeException {

    public ProjectEmployeeNotFoundException(Long employeeId, Long projectId) {
        super("Mitarbeiter mit der ID " + employeeId
                + " ist dem Projekt mit der ID " + projectId + " nicht zugeordnet.");
    }
}
