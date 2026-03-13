package com.univalle.parkingmanagementservice.ingreso.repository;

import com.univalle.parkingmanagementservice.ingreso.entities.EstadoIngreso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoIngresoRepository extends JpaRepository<EstadoIngreso, Long> {
    Optional<EstadoIngreso> findByNombre(String nombre);
}
