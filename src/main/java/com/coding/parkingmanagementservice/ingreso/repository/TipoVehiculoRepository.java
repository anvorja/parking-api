package com.coding.parkingmanagementservice.ingreso.repository;

import com.coding.parkingmanagementservice.ingreso.entities.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, Long> {
}