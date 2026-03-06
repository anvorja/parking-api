package com.univalle.parkingmanagementservice.usuario.controller;

import java.util.List;

import com.univalle.parkingmanagementservice.usuario.dto.UsuarioListItemResponse;
import com.univalle.parkingmanagementservice.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas con usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Retorna el listado de usuarios registrados")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioListItemResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }
}
