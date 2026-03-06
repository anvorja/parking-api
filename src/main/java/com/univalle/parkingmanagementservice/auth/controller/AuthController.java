package com.univalle.parkingmanagementservice.auth.controller;

import com.univalle.parkingmanagementservice.auth.dto.LoginRequest;
import com.univalle.parkingmanagementservice.auth.dto.LoginResponse;
import com.univalle.parkingmanagementservice.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Solicitud de inicio de sesión..");
        return ResponseEntity.ok(authService.login(request));
    }
}
