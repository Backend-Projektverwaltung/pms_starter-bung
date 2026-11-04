package de.szut.pms.project;

public class EmployeeQualificationNotFoundException extends RuntimeException {

    public EmployeeQualificationNotFoundException(String qualification, Long employeeId) {
        super("Der Mitarbeiter mit der ID " + employeeId
                + " besitzt nicht die Qualifikation \"" + qualification + "\".");
    }
}
