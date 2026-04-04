package com.coding.parkingmanagementservice.ingreso.repository;

import com.coding.parkingmanagementservice.ingreso.entities.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    /**
     * Lista solo las ubicaciones activas (DISPONIBLE u OCUPADO).
     * Excluye las INACTIVAS (soft-deleted).
     * Usado por el GET /api/v1/ubicaciones.
     */
    @Query("""
        SELECT u FROM Ubicacion u
        WHERE UPPER(u.estadoUbicacion.nombre) != 'INACTIVO'
        ORDER BY u.nombre ASC
    """)
    List<Ubicacion> findAllActivas();

    /**
     * Lista todas las ubicaciones, incluyendo las inactivas.
     * Usado por el backend para la vista de mantenimiento.
     */
    @Query("""
        SELECT u FROM Ubicacion u
        ORDER BY u.nombre ASC
    """)
    List<Ubicacion> findAllUbicaciones();

    /**
     * Verifica si el nombre ya existe — para validar unicidad al crear o editar.
     * Excluye la propia ubicación al editar.
     */
    @Query("""
        SELECT COUNT(u) > 0 FROM Ubicacion u
        WHERE UPPER(u.nombre) = UPPER(:nombre)
          AND (:idExcluir IS NULL OR u.id != :idExcluir)
    """)
    boolean existeNombre(
            @Param("nombre") String nombre,
            @Param("idExcluir") Long idExcluir
    );

    /**
     * Verifica si la ubicación tiene ingresos activos (sin fecha de salida).
     * Usado para bloquear la edición de tipo y la eliminación.
     */
    @Query("""
        SELECT COUNT(i) > 0 FROM IngresoVehiculo i
        WHERE i.ubicacion.id = :idUbicacion
          AND i.fechaHoraSalida IS NULL
    """)
    boolean tieneIngresosActivos(@Param("idUbicacion") Long idUbicacion);
}