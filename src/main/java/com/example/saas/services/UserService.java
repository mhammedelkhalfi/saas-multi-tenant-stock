package com.example.saas.services;

import com.example.saas.common.PageResponse;
import com.example.saas.request.UserRequest;
import com.example.saas.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void createUser(final UserRequest request);
    void updateUser(final String id, final UserRequest request);
    void deleteUser(final String id);
    UserResponse getUserById(final String id);
    PageResponse<UserResponse> getAllUser(final int page,final int size);
    void enableUser(final String id);
    void disableUser(final String id);

}
