package com.univalle.parkingmanagementservice.auth.service;

import com.univalle.parkingmanagementservice.auth.dto.LoginRequest;
import com.univalle.parkingmanagementservice.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
