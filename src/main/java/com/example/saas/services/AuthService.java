package com.example.saas.services;

import com.example.saas.request.LoginRequest;
import com.example.saas.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse refresh(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    AuthResponse me();
}
