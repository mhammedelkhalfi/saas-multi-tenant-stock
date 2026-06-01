package com.example.saas.services.impl;

import com.example.saas.common.PageResponse;
import com.example.saas.entities.Categorie;
import com.example.saas.exceptions.DuplicateResouceException;
import com.example.saas.exceptions.ResourceNotFoundException;
import com.example.saas.mappers.CategorieMapper;
import com.example.saas.request.CategorieRequest;
import com.example.saas.response.CategorieResponse;
import com.example.saas.respositories.CategorieRepositorie;
import com.example.saas.services.CategorieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategorieServiceImpl implements CategorieService {


    @Autowired
    private final CategorieRepositorie categorieRepositorie;
    private final CategorieMapper categorieMapper;

    @Override
    public void create(CategorieRequest request) {
        // check if categorie existe
        checkIfCategorieExiste(request.getName());
        final Categorie categorie =categorieMapper.toEntity(request);
        this.categorieRepositorie.save(categorie);

    }



    @Override
    public void update(String id, CategorieRequest request) {
        final Optional<Categorie> existingCategorie = this.categorieRepositorie.findById(id);
        if (existingCategorie.isEmpty()){
            log.debug("Categorie Not Found..");
            throw new ResourceNotFoundException("Categorie Not Found..");
        }

        final Categorie categorie = existingCategorie.get();
        if (!categorie.getName().equalsIgnoreCase(request.getName()) == false){
            // check if categorie existe
            checkIfCategorieExiste(request.getName());
        }

        final Categorie updatedCategorie = categorieMapper.toEntity(request);
        updatedCategorie.setId(id);
        this.categorieRepositorie.save(updatedCategorie);
    }

    @Override
    public void delete(String id) {
        final Categorie categorie = this.categorieRepositorie.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie Not Found .."));
        this.categorieRepositorie.delete(categorie);
    }

    @Override
    public CategorieResponse get(String id) {
        return this.categorieRepositorie.findById(id)
                .map(this.categorieMapper::toRequest)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie Not Found.."));
    }

    @Override
    public PageResponse<CategorieResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(this.categorieRepositorie.findAll(pageable)
                .map(this.categorieMapper::toRequest));
    }

    @Override
    public PageResponse<CategorieResponse> search(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return getAll(page, size);
        }
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(this.categorieRepositorie.search(keyword.trim(), pageable)
                .map(this.categorieMapper::toRequest));
    }

    private void checkIfCategorieExiste(final String name) {
        final Optional<Categorie> categorie =this.categorieRepositorie.findByNameIgnoreCase(name);
        if (categorie.isPresent()){
            log.debug("Categorie already existe ");
            throw new DuplicateResouceException("Categorie already existe ");
        }
    }
}
