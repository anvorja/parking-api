package com.coding.parkingmanagementservice.ingreso.repository;

import com.coding.parkingmanagementservice.ingreso.entities.IngresoVehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IngresoVehiculoRepository extends JpaRepository<IngresoVehiculo, Long> {

    boolean existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull(String placa);

    /**
     * Cuenta ingresos activos (sin fecha de salida) en una ubicación.
     */
    @Query("""
        SELECT COUNT(i) FROM IngresoVehiculo i
        WHERE i.ubicacion.id = :idUbicacion
          AND i.fechaHoraSalida IS NULL
    """)
    int contarActivosPorUbicacion(@Param("idUbicacion") Long idUbicacion);

    /**
     * Verifica si existe un ingreso activo de un tipo de vehículo en la ubicación.
     */
    @Query("""
        SELECT COUNT(i) > 0 FROM IngresoVehiculo i
        WHERE i.ubicacion.id = :idUbicacion
          AND i.tipoVehiculo.id = :idTipoVehiculo
          AND i.fechaHoraSalida IS NULL
    """)
    boolean existeActivoConTipoVehiculo(
            @Param("idUbicacion") Long idUbicacion,
            @Param("idTipoVehiculo") Long idTipoVehiculo
    );

    @Query("""
        SELECT i FROM IngresoVehiculo i
        WHERE (:placa IS NULL OR :placa = '' OR UPPER(i.placa) LIKE UPPER(CONCAT('%', :placa, '%')))
          AND (:estado IS NULL OR :estado = '' OR UPPER(i.estadoIngreso.nombre) = UPPER(:estado))
          AND (CAST(:fechaInicio AS timestamp) IS NULL OR (i.fechaHoraIngreso >= :fechaInicio AND i.fechaHoraIngreso < :fechaFin))
        ORDER BY i.fechaHoraIngreso DESC
    """)
    Page<IngresoVehiculo> listarConFiltros(
            @Param("placa") String placa,
            @Param("estado") String estado,
            @Param("fechaInicio") java.time.OffsetDateTime fechaInicio,
            @Param("fechaFin") java.time.OffsetDateTime fechaFin,
            Pageable pageable
    );

    /**
     * HU-010 — Busca el ingreso activo (sin fecha de salida) para una placa.
     * Usado en la salida manual para ubicar el vehículo por placa.
     * Hace FETCH de todas las asociaciones lazy para evitar N+1 en el toResponse().
     */
    @Query("""
        SELECT i FROM IngresoVehiculo i
        JOIN FETCH i.tipoVehiculo
        JOIN FETCH i.ubicacion
        JOIN FETCH i.estadoIngreso
        JOIN FETCH i.usuarioRegistro
        WHERE UPPER(i.placa) = UPPER(:placa)
          AND i.fechaHoraSalida IS NULL
        ORDER BY i.fechaHoraIngreso DESC
    """)
    Optional<IngresoVehiculo> findActivoByPlaca(@Param("placa") String placa);

    /**
     * HU-009 — Obtiene un ingreso por id con todas sus asociaciones.
     * Usado para mostrar el detalle antes de confirmar la salida vía QR.
     */
    @Query("""
        SELECT i FROM IngresoVehiculo i
        JOIN FETCH i.tipoVehiculo
        JOIN FETCH i.ubicacion
        JOIN FETCH i.estadoIngreso
        JOIN FETCH i.usuarioRegistro
        WHERE i.id = :id
    """)
    Optional<IngresoVehiculo> findByIdFetchAll(@Param("id") Long id);
}