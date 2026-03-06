package com.univalle.parkingmanagementservice.auth.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "estado_usuario", schema = "public")
public class EstadoUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_usuario")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 30, unique = true)
    private String nombre;
}
