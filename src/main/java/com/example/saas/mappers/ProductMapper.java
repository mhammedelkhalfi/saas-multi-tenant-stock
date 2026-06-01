package com.example.saas.mappers;

import com.example.saas.entities.Categorie;
import com.example.saas.entities.Product;
import com.example.saas.request.ProductRequest;
import com.example.saas.response.ProductResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

    public Product toEntity(final ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .reference(request.getReference())
                .description(request.getDescription())
                .alertThreshold(request.getAlertThreshold())
                .price(request.getPrice())
                .categorie(request.getCategoryId() != null
                        ? Categorie.builder().id(request.getCategoryId()).build()
                        : null)
                .deleted(false)
                .build();
    }

    public ProductResponse toResponse(final Product entity, final int availableQuantity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .reference(entity.getReference())
                .description(entity.getDescription())
                .alertThreshold(entity.getAlertThreshold())
                .price(entity.getPrice())
                .categoryName(entity.getCategorie() != null ? entity.getCategorie().getName() : null)
                .availableQuantity(availableQuantity)
                .build();
    }
}
