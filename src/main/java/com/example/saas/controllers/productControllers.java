package com.example.saas.controllers;

import com.example.saas.common.PageResponse;
import com.example.saas.request.ProductRequest;
import com.example.saas.response.ProductResponse;
import com.example.saas.services.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/product")
@RequiredArgsConstructor
@Slf4j
@Tag( name = "Product", description = "Product API")
public class productControllers {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<Void> createProduct(
            @Valid
            @RequestBody
            final ProductRequest request
    ) {
        service.create(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{product_id}")
    public ResponseEntity<Void> updateProduct(
            @Valid
            @RequestBody
            final ProductRequest request,
            @PathVariable("product_id")
            final String id
    ) {
        service.update(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{product_id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("product_id")
            final String id
    ) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0")
            final int page,
            @RequestParam(defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(service.getAll(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @RequestParam
            final String keyword,
            @RequestParam(defaultValue = "0")
            final int page,
            @RequestParam(defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(service.search(keyword, page, size));
    }

    @GetMapping("/{product_id}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable("product_id")
            final String id
    ) {
        return ResponseEntity.ok(service.get(id));
    }
}
