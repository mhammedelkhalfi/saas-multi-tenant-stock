package com.example.saas.entities;

import com.example.saas.enums.TypeMouvement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "stock_mvts")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StockMvt extends AbstractEntity {

    @Column(name = "type_mvt", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeMouvement typeMvt;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "date_mvt", nullable = false)
    private LocalDate dateMvt;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
