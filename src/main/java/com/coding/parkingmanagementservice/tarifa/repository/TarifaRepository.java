package com.coding.parkingmanagementservice.tarifa.repository;

import com.coding.parkingmanagementservice.tarifa.entities.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    /** Todas las tarifas activas — para listado y caché frontend */
    List<Tarifa> findByActivaTrue();

    /**
     * Tarifa activa para un tipo de vehículo y unidad de tiempo específicos.
     * Usada en el cálculo de costo al confirmar la salida.
     */
    Optional<Tarifa> findByTipoVehiculo_IdAndUnidadTarifa_IdAndActivaTrue(
            Long idTipoVehiculo, Long idUnidadTarifa);

    /**
     * Desactiva todas las tarifas activas de un tipo+unidad antes de crear una nueva.
     * Garantiza que solo exista una tarifa activa por tipo+unidad en cualquier momento.
     */
    @Modifying
    @Query("""
        UPDATE Tarifa t SET t.activa = false
        WHERE t.tipoVehiculo.id = :idTipoVehiculo
          AND t.unidadTarifa.id = :idUnidadTarifa
          AND t.activa = true
    """)
    void desactivarActivasPorTipoYUnidad(
            @Param("idTipoVehiculo") Long idTipoVehiculo,
            @Param("idUnidadTarifa") Long idUnidadTarifa);
}