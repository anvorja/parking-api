package com.coding.parkingmanagementservice.auth.repositories;

import java.util.Optional;

import com.coding.parkingmanagementservice.auth.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);

    @Query("""
    SELECT u
    FROM Usuario u
    JOIN FETCH u.rol
    WHERE u.nombreUsuario = :nombreUsuario
""")
    Optional<Usuario> findByNombreUsuarioWithRol(@Param("nombreUsuario") String nombreUsuario);
}
