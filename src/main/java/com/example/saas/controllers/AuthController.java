package com.example.saas.controllers;

import com.example.saas.request.LoginRequest;
import com.example.saas.response.AuthResponse;
import com.example.saas.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, refresh token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody final LoginRequest request,
            final HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token (cookie or Bearer)")
    public ResponseEntity<AuthResponse> refresh(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke tokens")
    public ResponseEntity<Void> logout(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<AuthResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}
