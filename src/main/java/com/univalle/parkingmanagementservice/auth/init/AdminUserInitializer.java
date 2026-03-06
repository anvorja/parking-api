package com.univalle.parkingmanagementservice.auth.init;

import com.univalle.parkingmanagementservice.auth.entities.EstadoUsuario;
import com.univalle.parkingmanagementservice.auth.entities.Rol;
import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import com.univalle.parkingmanagementservice.auth.repositories.EstadoUsuarioRepository;
import com.univalle.parkingmanagementservice.auth.repositories.RolRepository;
import com.univalle.parkingmanagementservice.auth.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private static final String ADMIN_NOMBRE = "admin";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "1234";
    private static final String ROL_ADMIN = "ADMINISTRADOR";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EstadoUsuarioRepository estadoUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        long totalUsuarios = usuarioRepository.count();

        if (totalUsuarios > 0) {
            log.info("ya existen usuarios registrados, no se crea usuario administrador inicial.");
            return;
        }

        Rol rolAdmin = rolRepository.findByNombre(ROL_ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el rol requerido: " + ROL_ADMIN
                ));

        EstadoUsuario estadoActivo = estadoUsuarioRepository.findByNombre(ESTADO_ACTIVO)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el estado de usuario requerido: " + ESTADO_ACTIVO
                ));

        Usuario admin = new Usuario();
        admin.setNombreCompleto(ADMIN_NOMBRE);
        admin.setNombreUsuario(ADMIN_USERNAME);
        admin.setContrasenaHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRol(rolAdmin);
        admin.setEstadoUsuario(estadoActivo);
        admin.setFechaCreacion(OffsetDateTime.now());

        usuarioRepository.save(admin);

        log.info("Usuario administrador inicial creado correctamente.");
    }
}