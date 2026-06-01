package com.example.saas.controllers;

import com.example.saas.common.PageResponse;
import com.example.saas.request.StockMvtRequest;
import com.example.saas.response.StockMvtResponse;
import com.example.saas.services.StockMvtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/stock-mvt")
@RequiredArgsConstructor
@Slf4j
@Tag( name = "StockMvt", description = "StockMvt API")
public class stockMvtControllers {

    private final StockMvtService service;

    @PostMapping
    public ResponseEntity<Void> createStockMvt(
            @Valid
            @RequestBody
            final StockMvtRequest request
    ) {
        service.create(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{stock_mvt_id}")
    public ResponseEntity<Void> updateStockMvt(
            @Valid
            @RequestBody
            final StockMvtRequest request,
            @PathVariable("stock_mvt_id")
            final String id
    ) {
        service.update(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{stock_mvt_id}")
    public ResponseEntity<Void> deleteStockMvt(
            @PathVariable("stock_mvt_id")
            final String id
    ) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<StockMvtResponse>> getAllStockMvts(
            @RequestParam(defaultValue = "0")
            final int page,
            @RequestParam(defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(service.getAll(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<StockMvtResponse>> searchStockMvts(
            @RequestParam
            final String keyword,
            @RequestParam(defaultValue = "0")
            final int page,
            @RequestParam(defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(service.search(keyword, page, size));
    }

    @GetMapping("/{stock_mvt_id}")
    public ResponseEntity<StockMvtResponse> getStockMvt(
            @PathVariable("stock_mvt_id")
            final String id
    ) {
        return ResponseEntity.ok(service.get(id));
    }
}
