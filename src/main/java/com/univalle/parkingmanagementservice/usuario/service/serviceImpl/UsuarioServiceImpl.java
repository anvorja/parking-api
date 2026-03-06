package com.univalle.parkingmanagementservice.usuario.service.serviceImpl;

import java.util.Comparator;
import java.util.List;

import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import com.univalle.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.univalle.parkingmanagementservice.usuario.dto.UsuarioListItemResponse;
import com.univalle.parkingmanagementservice.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioListItemResponse> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Usuario::getNombreCompleto, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    private UsuarioListItemResponse toResponse(Usuario usuario) {
        return new UsuarioListItemResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getNombreUsuario(),
                usuario.getRol().getNombre()
        );
    }
}
