package com.example.saas.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter implements Filter {
    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain
    ) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final String tenantId = resolveHeader(request);
        try {
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setCurrentTenant(tenantId);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveHeader(HttpServletRequest request) {
        final String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId != null && !tenantId.isBlank()) {
            return tenantId.toLowerCase();
        }
        return null;
    }
}
