package com.univalle.parkingmanagementservice.ingreso.repository;

import com.univalle.parkingmanagementservice.ingreso.entities.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, Long> {
}