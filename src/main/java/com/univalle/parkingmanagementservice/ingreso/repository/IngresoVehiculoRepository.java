package com.univalle.parkingmanagementservice.ingreso.repository;

import com.univalle.parkingmanagementservice.ingreso.entities.IngresoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngresoVehiculoRepository extends JpaRepository<IngresoVehiculo, Long> {

    boolean existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull(String placa);

    boolean existsByUbicacion_IdAndFechaHoraSalidaIsNull(Long idUbicacion);
}
