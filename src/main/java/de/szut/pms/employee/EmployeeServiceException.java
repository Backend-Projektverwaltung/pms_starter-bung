package de.szut.pms.employee;

import org.springframework.http.HttpStatus;

public class EmployeeServiceException extends RuntimeException {

    private final HttpStatus status;

    public EmployeeServiceException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
