package com.company.pram.exception;

public class AllocationNotFoundException extends RuntimeException {
    public AllocationNotFoundException(Long id) {
        super("Allocation not found with id: " + id);
    }
}
