package com.coding.parkingmanagementservice.ingreso.repository;

import com.coding.parkingmanagementservice.ingreso.entities.EstadoUbicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoUbicacionRepository extends JpaRepository<EstadoUbicacion, Long> {
    Optional<EstadoUbicacion> findByNombre(String nombre);
}
