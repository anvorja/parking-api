package com.coding.parkingmanagementservice.ingreso.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estado_ubicacion")
@Getter
@Setter
public class EstadoUbicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_ubicacion")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 30, unique = true)
    private String nombre;
}
