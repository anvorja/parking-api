package com.coding.parkingmanagementservice.shared.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    public ConflictException(ErrorCode code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }
}
