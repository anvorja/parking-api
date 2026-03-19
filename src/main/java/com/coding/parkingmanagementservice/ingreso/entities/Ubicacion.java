package com.coding.parkingmanagementservice.ingreso.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ubicacion")
@Getter
@Setter
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Tipo de vehículo para el que fue diseñado físicamente este espacio.
     * CARRO → admite 1 carro, o hasta 4 motos (nunca ambos al mismo tiempo).
     * MOTO  → admite exclusivamente 1 moto.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_vehiculo_nativo", nullable = false)
    private TipoVehiculo tipoVehiculoNativo;

    /**
     * Cantidad máxima de vehículos que caben en este espacio físico.
     * Espacios de carro: 1 (un carro o hasta 4 motos, validado en servicio).
     * Espacios de moto: 1.
     */
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado_ubicacion", nullable = false)
    private EstadoUbicacion estadoUbicacion;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;
}