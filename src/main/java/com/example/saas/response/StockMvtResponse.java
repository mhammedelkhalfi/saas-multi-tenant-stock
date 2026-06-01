package com.example.saas.response;

import com.example.saas.enums.TypeMouvement;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMvtResponse {

    private String id;
    private TypeMouvement typeMvt;
    private Integer quantity;
    private LocalDate dateMvt;
    private String comment;
    private String productId;
    private String productName;
}
