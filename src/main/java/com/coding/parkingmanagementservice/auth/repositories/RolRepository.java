package com.coding.parkingmanagementservice.auth.repositories;

import java.util.Optional;

import com.coding.parkingmanagementservice.auth.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);
}