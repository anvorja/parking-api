package com.coding.parkingmanagementservice.auth.repositories;

import java.util.Optional;

import com.coding.parkingmanagementservice.auth.entities.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoUsuarioRepository extends JpaRepository<EstadoUsuario, Long> {

    Optional<EstadoUsuario> findByNombre(String nombre);
}
