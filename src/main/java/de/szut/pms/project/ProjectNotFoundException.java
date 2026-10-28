package de.szut.pms.project;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(Long id) {
        super("Projekt mit der ID " + id + " wurde nicht gefunden.");
    }
}