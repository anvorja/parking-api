package com.coding.parkingmanagementservice.auth.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entidad que persiste los refresh tokens en base de datos.
 * Permite revocarlos en el logout y verificar su vigencia.
 */
@Getter
@Setter
@Entity
@Table(name = "refresh_token", schema = "public")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_refresh_token")
    private Long id;

    /** El token opaco (UUID) que se entrega al cliente */
    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    /** Usuario al que pertenece este refresh token */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Momento en que expira — se comprueba en cada uso */
    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    /** true = el token fue revocado (logout explícito) */
    @Column(name = "revocado", nullable = false)
    private boolean revocado = false;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;
}