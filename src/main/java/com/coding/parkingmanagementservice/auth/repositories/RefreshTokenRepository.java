package com.coding.parkingmanagementservice.auth.repositories;

import com.coding.parkingmanagementservice.auth.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /** Revoca todos los refresh tokens activos de un usuario.
     *  Se llama en el logout para invalidar sesiones abiertas. */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revocado = true WHERE rt.usuario.id = :idUsuario AND rt.revocado = false")
    void revocarTodosPorUsuario(@Param("idUsuario") Long idUsuario);
}