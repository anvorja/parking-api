package com.coding.parkingmanagementservice.auth.service;

import com.coding.parkingmanagementservice.auth.dto.LoginRequest;
import com.coding.parkingmanagementservice.auth.dto.LoginResponse;
import com.coding.parkingmanagementservice.auth.dto.LogoutRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshResponse;

public interface AuthService {

    /** Autentica al usuario y devuelve access token + refresh token. */
    LoginResponse login(LoginRequest request);

    /**
     * Genera un nuevo access token a partir de un refresh token válido.
     * Lanza BusinessException si el token no existe, está revocado o expiró.
     */
    RefreshResponse refresh(RefreshRequest request);

    /**
     * Revoca el refresh token del usuario (logout explícito).
     * El access token sigue siendo válido hasta su expiración (15 min),
     * lo que es aceptable dado su corta duración.
     */
    void logout(LogoutRequest request);
}