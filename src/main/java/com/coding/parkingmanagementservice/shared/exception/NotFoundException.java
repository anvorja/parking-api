package com.coding.parkingmanagementservice.shared.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    public NotFoundException(ErrorCode code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }
}