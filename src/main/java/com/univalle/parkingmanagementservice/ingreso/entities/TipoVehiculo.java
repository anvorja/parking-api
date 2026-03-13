package com.univalle.parkingmanagementservice.ingreso.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipo_vehiculo")
@Getter
@Setter
public class TipoVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_vehiculo")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 30, unique = true)
    private String nombre;
}
