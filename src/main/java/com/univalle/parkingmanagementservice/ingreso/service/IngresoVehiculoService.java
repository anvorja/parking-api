package com.univalle.parkingmanagementservice.ingreso.service;

import com.univalle.parkingmanagementservice.ingreso.dto.IngresoVehiculoResponse;
import com.univalle.parkingmanagementservice.ingreso.dto.RegistrarIngresoRequest;

public interface IngresoVehiculoService {
    IngresoVehiculoResponse registrarIngreso(RegistrarIngresoRequest request, String nombreUsuarioAutenticado);
}
