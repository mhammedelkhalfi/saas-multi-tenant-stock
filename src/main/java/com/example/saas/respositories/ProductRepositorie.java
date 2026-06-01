package com.example.saas.respositories;

import com.example.saas.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepositorie extends JpaRepository<Product, String> {

    Optional<Product> findByReferenceIgnoreCase(String reference);

    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);
}
