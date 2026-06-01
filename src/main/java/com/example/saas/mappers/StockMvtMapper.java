package com.example.saas.mappers;

import com.example.saas.entities.Product;
import com.example.saas.entities.StockMvt;
import com.example.saas.request.StockMvtRequest;
import com.example.saas.response.StockMvtResponse;
import org.springframework.stereotype.Service;

@Service
public class StockMvtMapper {

    public StockMvt toEntity(final StockMvtRequest request) {
        return StockMvt.builder()
                .typeMvt(request.getTypeMvt())
                .quantity(request.getQuantity())
                .dateMvt(request.getDateMvt())
                .comment(request.getComment())
                .product(Product.builder()
                        .id(request.getProductId())
                        .build())
                .deleted(false)
                .build();
    }

    public StockMvtResponse toResponse(final StockMvt entity) {
        return StockMvtResponse.builder()
                .id(entity.getId())
                .typeMvt(entity.getTypeMvt())
                .quantity(entity.getQuantity())
                .dateMvt(entity.getDateMvt())
                .comment(entity.getComment())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .build();
    }
}
