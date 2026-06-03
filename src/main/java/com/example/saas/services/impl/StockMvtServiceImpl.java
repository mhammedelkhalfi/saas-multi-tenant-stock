package com.example.saas.services.impl;

import com.example.saas.common.PageResponse;
import com.example.saas.config.TenantContext;
import com.example.saas.entities.Product;
import com.example.saas.entities.StockMvt;
import com.example.saas.enums.PriorityType;
import com.example.saas.enums.TypeNotification;
import com.example.saas.exceptions.ResourceNotFoundException;
import com.example.saas.mappers.StockMvtMapper;
import com.example.saas.request.NotificationRequest;
import com.example.saas.request.StockMvtRequest;
import com.example.saas.response.StockMvtResponse;
import com.example.saas.respositories.ProductRepositorie;
import com.example.saas.respositories.StockMvtRepositorie;
import com.example.saas.services.NotificationService;
import com.example.saas.services.StockMvtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final NotificationService notificationService;

    @Override
    public void create(StockMvtRequest request) {
        final Product product = productRepositorie.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found.."));
        final StockMvt stockMvt = stockMvtMapper.toEntity(request);
        stockMvt.setProduct(product);
        final StockMvt saved = stockMvtRepositorie.save(stockMvt);
        notifyStockMovement(saved, product);
    }

    private void notifyStockMovement(final StockMvt stockMvt, final Product product) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        final String userId = authentication.getName();
        final String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return;
        }

        notificationService.sendToUser(userId, NotificationRequest.builder()
                .tenantId(tenantId)
                .typeNotification(TypeNotification.STOCK_MOVEMENT)
                .title("Mouvement de stock")
                .message(String.format(
                        "%s : %d unités — produit « %s »",
                        stockMvt.getTypeMvt(),
                        stockMvt.getQuantity(),
                        product.getName()
                ))
                .resourceType("STOCK_MVT")
                .resourceId(stockMvt.getId())
                .priority(PriorityType.MEDIUM)
                .build());
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
