package com.example.saas.respositories;

import com.example.saas.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepositorie extends JpaRepository<User, String> {

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleted = false")
    Optional<User> findByIdAndNotDeleted(String id);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tenant WHERE u.username = :username AND u.deleted = false")
    Optional<User> findActiveByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tenant WHERE u.id = :id AND u.deleted = false")
    Optional<User> findActiveById(@Param("id") String id);

    boolean existsByUsername(String adminUsername);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.deleted = false")
    Page<User> findAllByTenantId(String tenantId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.deleted = false AND u.enabled = true")
    List<User> findAllActiveByTenantId(@Param("tenantId") String tenantId);
}
