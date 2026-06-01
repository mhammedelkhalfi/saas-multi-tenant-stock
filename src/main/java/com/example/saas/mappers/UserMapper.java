package com.example.saas.mappers;

import com.example.saas.entities.User;
import com.example.saas.request.UserRequest;
import com.example.saas.response.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public User toEntity(UserRequest request) {
        return User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .role(request.getRole())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
    }

    public UserResponse toResponse(final User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
