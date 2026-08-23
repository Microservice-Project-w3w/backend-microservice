package com.equipmentrental.maintenance.exception;

public class BusinessException
        extends RuntimeException {

    public BusinessException(
            String message
    ) {
        super(message);
    }
}