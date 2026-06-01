package com.example.saas.request;

import com.example.saas.enums.TypeMouvement;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMvtRequest {
    private TypeMouvement typeMvt;
    private Integer quantity;
    private LocalDate dateMvt;
    private String comment;
    private String productId;
}
