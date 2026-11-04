package de.szut.pms.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Kunde mit ID " + customerId + " wurde nicht gefunden.");
    }
}