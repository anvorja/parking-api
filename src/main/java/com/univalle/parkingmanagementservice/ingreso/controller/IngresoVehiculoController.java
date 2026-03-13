package com.univalle.parkingmanagementservice.ingreso.controller;

import com.univalle.parkingmanagementservice.ingreso.dto.IngresoVehiculoResponse;
import com.univalle.parkingmanagementservice.ingreso.dto.RegistrarIngresoRequest;
import com.univalle.parkingmanagementservice.ingreso.service.IngresoVehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoVehiculoController {

    private final IngresoVehiculoService ingresoVehiculoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngresoVehiculoResponse registrarIngreso(
            @Valid @RequestBody RegistrarIngresoRequest request,
            Authentication authentication
    ) {
        return ingresoVehiculoService.registrarIngreso(request, authentication.getName());
    }
}
