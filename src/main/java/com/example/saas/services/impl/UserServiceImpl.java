package com.example.saas.services.impl;

import com.example.saas.common.PageResponse;
import com.example.saas.config.TenantContext;
import com.example.saas.entities.Tenant;
import com.example.saas.entities.User;
import com.example.saas.enums.UserRole;
import com.example.saas.exceptions.DuplicateResouceException;
import com.example.saas.exceptions.InvalidRequestException;
import com.example.saas.mappers.UserMapper;
import com.example.saas.request.UserRequest;
import com.example.saas.response.UserResponse;
import com.example.saas.respositories.UserRepositorie;
import com.example.saas.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepositorie userRepositorie;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createUser(UserRequest request) {
        final   String tenantId = TenantContext.getCurrentTenant();

        if (this.userRepositorie.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (this.userRepositorie.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if ( request.getRole() == UserRole.ROLE_PLATFORM_ADMIN  ){
            throw new InvalidRequestException("Platform admin can't be created");
        }

        final User user = userMapper.toEntity(request);
        user.setTenant(Tenant.builder().id(tenantId).build());
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        this.userRepositorie.save(user);
        log.info("User created successfully");
    }

    @Override
    public void updateUser(String id, UserRequest request) {

        final String tenantId = TenantContext.getCurrentTenant();
        log.info("updating user for Tenant id: {}", tenantId);
        final User user = this.userRepositorie.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getTenant().getId().equals(tenantId)) {
            throw new InvalidRequestException("User can't be updated");
        }
        if (!user.getUsername().equals(request.getUsername()) && this.userRepositorie.existsByUsername(request.getUsername())) {
            throw new DuplicateResouceException("Username already exists");
        }
        if (!user.getEmail().equals(request.getEmail()) && this.userRepositorie.existsByEmail(request.getEmail())) {
            throw new DuplicateResouceException("Email already exists");
        }
        if(request.getRole()== UserRole.ROLE_PLATFORM_ADMIN){
            throw new InvalidRequestException("Role can't be updated");
        }
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        this.userRepositorie.save(user);
        log.info("User updated successfully");
    }

    @Override
    public void deleteUser(String id) {
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting user for tenant: {}", tenantId);

        final User user = this.userRepositorie.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new InvalidRequestException("User can't be deleted");
        }
        user.setDeleted(true);
        this.userRepositorie.save(user);
        log.info("User deleted successfully");

    }

    @Override
    public UserResponse getUserById(String id) {
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting user for tenant: {}", tenantId);
        final User user = this.userRepositorie.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new InvalidRequestException("User can't be retrieved");
        }
        return this.userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getAllUser(int page, int size) {
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all users for tenant: {}", tenantId);
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<User> userPage = this.userRepositorie.findAllByTenantId(tenantId, pageRequest);
        final Page<UserResponse> userResponsePage = userPage.map(this.userMapper::toResponse);
        return PageResponse.of(userResponsePage);
    }

    @Override
    public void enableUser(String id) {
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Enabling user for tenant: {}", tenantId);
        final User user = this.userRepositorie.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!user.getTenant().getId().equals(tenantId)){
            throw new InvalidRequestException("User can't be enabled");
        }
        user.setEnabled(true);
        this.userRepositorie.save(user);
        log.info("User enabled successfully");
    }

    @Override
    public void disableUser(String id) {
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Enabling user for tenant: {}", tenantId);
        final User user = this.userRepositorie.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!user.getTenant().getId().equals(tenantId)){
            throw new InvalidRequestException("User can't be enabled");
        }
        user.setEnabled(false);
        this.userRepositorie.save(user);
        log.info("User enabled successfully");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepositorie.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found.."));
    }
}
