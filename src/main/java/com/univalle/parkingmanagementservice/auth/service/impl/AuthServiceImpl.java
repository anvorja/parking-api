package com.univalle.parkingmanagementservice.auth.service.impl;

import com.univalle.parkingmanagementservice.auth.dto.AuthenticatedUserResponse;
import com.univalle.parkingmanagementservice.auth.dto.LoginRequest;
import com.univalle.parkingmanagementservice.auth.dto.LoginResponse;
import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import com.univalle.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.univalle.parkingmanagementservice.auth.service.AuthService;
import com.univalle.parkingmanagementservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales no válidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getContrasenaHash())) {
            throw new BadCredentialsException("Credenciales no válidas");
        }

        String token = jwtService.generateToken(usuario);

        AuthenticatedUserResponse usuarioResponse = new AuthenticatedUserResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getNombreUsuario(),
                usuario.getRol().getNombre()
        );

        return new LoginResponse(token, "Bearer", usuarioResponse);
    }
}
