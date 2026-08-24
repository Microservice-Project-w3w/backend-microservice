package com.equipmentrental.maintenance.exception;

public class ForbiddenException
        extends RuntimeException {

    public ForbiddenException(
            String message
    ) {
        super(message);
    }
}