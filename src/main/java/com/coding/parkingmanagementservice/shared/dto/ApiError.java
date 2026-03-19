package com.coding.parkingmanagementservice.shared.dto;

public record ApiError(
        String code,
        String message
) {
}