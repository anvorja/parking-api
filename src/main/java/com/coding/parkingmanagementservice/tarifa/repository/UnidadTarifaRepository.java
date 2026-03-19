package com.coding.parkingmanagementservice.tarifa.repository;

import com.coding.parkingmanagementservice.tarifa.entities.UnidadTarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnidadTarifaRepository extends JpaRepository<UnidadTarifa, Long> {
    Optional<UnidadTarifa> findByNombre(String nombre);
}