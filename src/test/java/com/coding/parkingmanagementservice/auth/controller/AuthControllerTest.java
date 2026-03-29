package com.coding.parkingmanagementservice.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.coding.parkingmanagementservice.auth.dto.LoginRequest;
import com.coding.parkingmanagementservice.auth.dto.LoginResponse;
import com.coding.parkingmanagementservice.auth.dto.LogoutRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshResponse;
import com.coding.parkingmanagementservice.auth.service.AuthService;
import com.coding.parkingmanagementservice.usuario.dto.MensajeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_deberiaRetornarResponseEntityOkConLoginResponse() {
        LoginRequest request = mock(LoginRequest.class);
        LoginResponse loginResponse = mock(LoginResponse.class);

        when(authService.login(request)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertSame(loginResponse, response.getBody());

        verify(authService).login(request);
    }

    @Test
    void refresh_deberiaRetornarResponseEntityOkConRefreshResponse() {
        RefreshRequest request = mock(RefreshRequest.class);
        RefreshResponse refreshResponse = mock(RefreshResponse.class);

        when(authService.refresh(request)).thenReturn(refreshResponse);

        ResponseEntity<RefreshResponse> response = authController.refresh(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertSame(refreshResponse, response.getBody());

        verify(authService).refresh(request);
    }

    @Test
    void logout_deberiaInvocarServicioYRetornarMensajeCorrecto() {
        LogoutRequest request = mock(LogoutRequest.class);

        ResponseEntity<MensajeResponse> response = authController.logout(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sesión cerrada correctamente", response.getBody().mensaje());

        verify(authService).logout(request);
    }
}