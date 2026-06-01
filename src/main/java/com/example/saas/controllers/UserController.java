package com.example.saas.controllers;

import com.example.saas.common.PageResponse;
import com.example.saas.request.UserRequest;
import com.example.saas.response.UserResponse;
import com.example.saas.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> createUser(
            @Valid
            @RequestBody
            final UserRequest request) {
        this.userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMINISTRATOR')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        final PageResponse<UserResponse> response = this.userService.getAllUser(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{user-id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMINISTRATOR')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("user-id")
            final String id) {
        final UserResponse response = this.userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{user-id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> updateUser(
            @PathVariable("user-id")
            final String id,
            @Valid
            @RequestBody
            final UserRequest request) {
        this.userService.updateUser(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }

    @DeleteMapping("/{user-id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("user-id")
            final String id) {
        this.userService.deleteUser(id);
        return ResponseEntity.noContent()
                .build();
    }

    @PutMapping("/{user-id}/enable")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> enableUser(
            @PathVariable("user-id")
            final String id) {
        this.userService.enableUser(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }

    @PutMapping("/{user-id}/disable")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> disableUser(
            @PathVariable("user-id")
            final String id) {
        this.userService.disableUser(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }
}
