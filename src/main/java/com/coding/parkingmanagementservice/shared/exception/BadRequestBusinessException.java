package com.coding.parkingmanagementservice.shared.exception;

import org.springframework.http.HttpStatus;

public class BadRequestBusinessException extends BusinessException {
    public BadRequestBusinessException(ErrorCode code, String message) {
        super(code, message, HttpStatus.BAD_REQUEST);
    }
}