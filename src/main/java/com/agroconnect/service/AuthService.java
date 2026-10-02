package com.agroconnect.service;

import com.agroconnect.dto.JwtAuthResponse;
import com.agroconnect.dto.LoginDto;
import com.agroconnect.dto.RegisterDto;

public interface AuthService {
    String register(RegisterDto registerDto);
    JwtAuthResponse login(LoginDto loginDto);
}
