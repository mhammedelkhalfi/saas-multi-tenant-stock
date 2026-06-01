package com.example.saas.services.impl;

import com.example.saas.common.PageResponse;
import com.example.saas.entities.Categorie;
import com.example.saas.entities.Product;
import com.example.saas.exceptions.DuplicateResouceException;
import com.example.saas.exceptions.ResourceNotFoundException;
import com.example.saas.mappers.ProductMapper;
import com.example.saas.request.ProductRequest;
import com.example.saas.response.ProductResponse;
import com.example.saas.respositories.CategorieRepositorie;
import com.example.saas.respositories.ProductRepositorie;
import com.example.saas.respositories.StockMvtRepositorie;
import com.example.saas.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepositorie productRepositorie;
    private final CategorieRepositorie categorieRepositorie;
    private final StockMvtRepositorie stockMvtRepositorie;
    private final ProductMapper productMapper;

    @Override
    public void create(ProductRequest request) {
        checkIfReferenceExists(request.getReference());
        final Categorie categorie = getCategorie(request.getCategoryId());
        final Product product = productMapper.toEntity(request);
        product.setCategorie(categorie);
        productRepositorie.save(product);
    }

    @Override
    public void update(String id, ProductRequest request) {
        final Product existingProduct = productRepositorie.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found.."));

        if (!existingProduct.getReference().equalsIgnoreCase(request.getReference())) {
            checkIfReferenceExists(request.getReference());
        }

        existingProduct.setName(request.getName());
        existingProduct.setReference(request.getReference());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setAlertThreshold(request.getAlertThreshold());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setCategorie(getCategorie(request.getCategoryId()));
        productRepositorie.save(existingProduct);
    }

    @Override
    public void delete(String id) {
        final Product product = productRepositorie.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found.."));
        productRepositorie.delete(product);
    }

    @Override
    public ProductResponse get(String id) {
        final Product product = productRepositorie.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found.."));
        return toResponse(product);
    }

    @Override
    public PageResponse<ProductResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(productRepositorie.findAll(pageable)
                .map(this::toResponse));
    }

    @Override
    public PageResponse<ProductResponse> search(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return getAll(page, size);
        }
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(productRepositorie.search(keyword.trim(), pageable)
                .map(this::toResponse));
    }

    private ProductResponse toResponse(final Product product) {
        final int availableQuantity = stockMvtRepositorie.getAvailableQuantity(product.getId()).intValue();
        return productMapper.toResponse(product, availableQuantity);
    }

    private Categorie getCategorie(final String categoryId) {
        return categorieRepositorie.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie Not Found.."));
    }

    private void checkIfReferenceExists(final String reference) {
        final Optional<Product> product = productRepositorie.findByReferenceIgnoreCase(reference);
        if (product.isPresent()) {
            log.debug("Product reference already exists");
            throw new DuplicateResouceException("Product reference already exists");
        }
    }
}
