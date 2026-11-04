package de.szut.pms.employee;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long employeeId) {
        super("Mitarbeiter mit der ID " + employeeId + " wurde nicht gefunden.");
    }
}
