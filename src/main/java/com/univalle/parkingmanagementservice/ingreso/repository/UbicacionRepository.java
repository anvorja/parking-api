package com.univalle.parkingmanagementservice.ingreso.repository;

import com.univalle.parkingmanagementservice.ingreso.entities.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
}
