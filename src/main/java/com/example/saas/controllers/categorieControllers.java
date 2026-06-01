package com.example.saas.controllers;

import com.example.saas.common.PageResponse;
import com.example.saas.request.CategorieRequest;
import com.example.saas.response.CategorieResponse;
import com.example.saas.services.CategorieService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/categorie")
@RequiredArgsConstructor
@Slf4j
@Tag( name = "Categorie", description = "Categorie API")
public class categorieControllers {

    private final CategorieService service;

    @PostMapping
    public ResponseEntity<Void> createCategorie(
            @Valid
            @RequestBody
            final CategorieRequest request
            ){
        service.create(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateCategorie(
            @Valid
            @RequestBody
            final CategorieRequest request,
            @PathVariable("categorie_id")
            final String id

    ){
        service.update(id,request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{categorie_id}")
    public ResponseEntity<Void> deleteCategorie(
            @PathVariable("categorie_id")
            final String id
    ){
        service.delete(id);
        return null;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CategorieResponse>> getAllCategorie(
            @RequestParam(defaultValue = "0")
            final int page,
            @RequestParam(defaultValue = "10")
            final int size
    ){
        return ResponseEntity.ok(service.getAll(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<CategorieResponse>> searchCategorie(
            @RequestParam
            final String keyword,
            @RequestParam(defaultValue = "0")
            final int page,
            @RequestParam(defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(service.search(keyword, page, size));
    }

    @GetMapping("/{categorie_id}")
    public ResponseEntity<CategorieResponse> getCategorie(
            @PathVariable("categorie_id")
            final String id
    ){
        return ResponseEntity.ok(service.get(id));
    }



}
