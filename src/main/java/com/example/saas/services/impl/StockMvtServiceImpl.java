package com.example.saas.services.impl;

import com.example.saas.common.PageResponse;
import com.example.saas.entities.Product;
import com.example.saas.entities.StockMvt;
import com.example.saas.exceptions.ResourceNotFoundException;
import com.example.saas.mappers.StockMvtMapper;
import com.example.saas.request.StockMvtRequest;
import com.example.saas.response.StockMvtResponse;
import com.example.saas.respositories.ProductRepositorie;
import com.example.saas.respositories.StockMvtRepositorie;
import com.example.saas.services.StockMvtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockMvtServiceImpl implements StockMvtService {

    private final StockMvtRepositorie stockMvtRepositorie;
    private final ProductRepositorie productRepositorie;
    private final StockMvtMapper stockMvtMapper;

    @Override
    public void create(StockMvtRequest request) {
        final Product product = productRepositorie.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found.."));
        final StockMvt stockMvt = stockMvtMapper.toEntity(request);
        stockMvt.setProduct(product);
        stockMvtRepositorie.save(stockMvt);
    }

    @Override
    public void update(String id, StockMvtRequest request) {
        final StockMvt existingStockMvt = stockMvtRepositorie.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement Not Found.."));

        final Product product = productRepositorie.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found.."));

        existingStockMvt.setTypeMvt(request.getTypeMvt());
        existingStockMvt.setQuantity(request.getQuantity());
        existingStockMvt.setDateMvt(request.getDateMvt());
        existingStockMvt.setComment(request.getComment());
        existingStockMvt.setProduct(product);
        stockMvtRepositorie.save(existingStockMvt);
    }

    @Override
    public void delete(String id) {
        final StockMvt stockMvt = stockMvtRepositorie.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement Not Found.."));
        stockMvtRepositorie.delete(stockMvt);
    }

    @Override
    public StockMvtResponse get(String id) {
        return stockMvtRepositorie.findById(id)
                .map(stockMvtMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement Not Found.."));
    }

    @Override
    public PageResponse<StockMvtResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(stockMvtRepositorie.findAll(pageable)
                .map(stockMvtMapper::toResponse));
    }

    @Override
    public PageResponse<StockMvtResponse> search(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return getAll(page, size);
        }
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(stockMvtRepositorie.search(keyword.trim(), pageable)
                .map(stockMvtMapper::toResponse));
    }
}
