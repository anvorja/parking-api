package com.coding.parkingmanagementservice.ingreso.dto;

import java.util.List;

public record IngresoVehiculoPageResponse(
        List<IngresoVehiculoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
