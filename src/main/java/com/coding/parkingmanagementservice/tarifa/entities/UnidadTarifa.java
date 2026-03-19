package com.coding.parkingmanagementservice.tarifa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "unidad_tarifa")
@Getter
@Setter
public class UnidadTarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad_tarifa")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 20, unique = true)
    private String nombre;
}