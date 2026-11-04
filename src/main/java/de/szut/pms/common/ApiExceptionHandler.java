package de.szut.pms.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.employee.EmployeeServiceException;
import de.szut.pms.hello.HelloNotFoundException;
import de.szut.pms.project.EmployeeQualificationNotFoundException;
import de.szut.pms.project.ProjectAssignmentConflictException;
import de.szut.pms.project.ProjectNotFoundException;
import de.szut.pms.project.ProjectEmployeeNotFoundException;
import de.szut.pms.customer.CustomerNotFoundException;

/**
 * Zentrale Übersetzung von Ausnahmen in HTTP-Antworten nach RFC 9457
 * (ProblemDetail). Eine Methode je Ausnahmetyp, wie in Tutorial 4, AB 03.
 * <p>
 * Ergänze hier eigene {@code @ExceptionHandler}-Methoden für die fachlichen
 * Ausnahmen deiner Ressourcen — nach demselben Muster wie
 * {@link #handleNotFound}.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(HelloNotFoundException.class)
    public ProblemDetail handleNotFound(HelloNotFoundException ex) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Nicht gefunden");
        return problem;
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ProblemDetail handleProjectNotFound(ProjectNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Projekt nicht gefunden");
        problem.setProperty("code", "PROJECT_NOT_FOUND");
        return problem;
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFound(CustomerNotFoundException ex) {
        return problem(
                HttpStatus.NOT_FOUND,
                "Kunde nicht gefunden",
                ex.getMessage(),
                "CUSTOMER_NOT_FOUND"
        );
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail handleEmployeeNotFound(EmployeeNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Mitarbeiter nicht gefunden", ex.getMessage(),
                "EMPLOYEE_NOT_FOUND");
    }

    @ExceptionHandler(ProjectEmployeeNotFoundException.class)
    public ProblemDetail handleProjectEmployeeNotFound(ProjectEmployeeNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Projektzuordnung nicht gefunden", ex.getMessage(),
                "PROJECT_EMPLOYEE_NOT_FOUND");
    }

    @ExceptionHandler(EmployeeQualificationNotFoundException.class)
    public ProblemDetail handleEmployeeQualificationNotFound(EmployeeQualificationNotFoundException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Qualifikation nicht vorhanden", ex.getMessage(),
                "EMPLOYEE_QUALIFICATION_NOT_FOUND");
    }

    @ExceptionHandler(ProjectAssignmentConflictException.class)
    public ProblemDetail handleProjectAssignmentConflict(ProjectAssignmentConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Projektzuordnung nicht möglich", ex.getMessage(),
                "PROJECT_ASSIGNMENT_CONFLICT");
    }

    @ExceptionHandler(EmployeeServiceException.class)
    public ProblemDetail handleEmployeeServiceFailure(EmployeeServiceException ex) {
        return problem(ex.getStatus(), "Employee-Service nicht verfügbar", ex.getMessage(),
                "EMPLOYEE_SERVICE_FAILURE");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Ungültige Eingabe");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Die Anfrage enthält ungültige Felder.");
        problem.setTitle("Ungültige Eingabe");
        problem.setProperty("errors", errors);
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        return problem;
    }
}