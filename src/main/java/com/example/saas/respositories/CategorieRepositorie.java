package com.example.saas.respositories;


import com.example.saas.entities.Categorie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategorieRepositorie extends JpaRepository<Categorie,String> {
    Optional<Categorie> findByNameIgnoreCase(String name);

    @Query("""
            SELECT c FROM Categorie c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Categorie> search(@Param("keyword") String keyword, Pageable pageable);
}
