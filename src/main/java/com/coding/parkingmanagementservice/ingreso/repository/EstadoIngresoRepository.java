package com.coding.parkingmanagementservice.ingreso.repository;

import com.coding.parkingmanagementservice.ingreso.entities.EstadoIngreso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoIngresoRepository extends JpaRepository<EstadoIngreso, Long> {
    Optional<EstadoIngreso> findByNombre(String nombre);
}
