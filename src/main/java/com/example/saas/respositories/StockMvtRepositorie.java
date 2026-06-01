package com.example.saas.respositories;

import com.example.saas.entities.StockMvt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMvtRepositorie extends JpaRepository<StockMvt, String> {

    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN s.typeMvt = com.example.saas.enums.TypeMouvement.IN THEN s.quantity
                     ELSE -s.quantity END
            ), 0)
            FROM StockMvt s
            WHERE s.product.id = :productId
            """)
    Long getAvailableQuantity(@Param("productId") String productId);

    @Query("""
            SELECT s FROM StockMvt s
            LEFT JOIN s.product p
            WHERE LOWER(s.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<StockMvt> search(@Param("keyword") String keyword, Pageable pageable);
}
