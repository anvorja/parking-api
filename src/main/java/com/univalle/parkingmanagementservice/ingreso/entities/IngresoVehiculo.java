package com.univalle.parkingmanagementservice.ingreso.entities;

import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ingreso_vehiculo")
@Getter
@Setter
public class IngresoVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso")
    private Long id;

    @Column(name = "placa", nullable = false, length = 10)
    private String placa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_vehiculo", nullable = false)
    private TipoVehiculo tipoVehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ubicacion", nullable = false)
    private Ubicacion ubicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado_ingreso", nullable = false)
    private EstadoIngreso estadoIngreso;

    @Column(name = "fecha_hora_ingreso", nullable = false)
    private OffsetDateTime fechaHoraIngreso;

    @Column(name = "fecha_hora_salida")
    private OffsetDateTime fechaHoraSalida;

    @Column(name = "valor_cobrado", precision = 12, scale = 2)
    private BigDecimal valorCobrado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_registro", nullable = false)
    private Usuario usuarioRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_entrega")
    private Usuario usuarioEntrega;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;
}
