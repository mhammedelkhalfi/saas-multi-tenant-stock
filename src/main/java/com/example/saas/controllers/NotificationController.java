package com.example.saas.controllers;

import com.example.saas.common.PageResponse;
import com.example.saas.response.NotificationResponse;
import com.example.saas.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notifications REST + WebSocket temps réel")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Liste des notifications de l'utilisateur connecté")
    public ResponseEntity<PageResponse<NotificationResponse>> getMyNotifications(
            final Authentication authentication,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(
                notificationService.getUserNotifications(authentication.getName(), page, size)
        );
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Nombre de notifications non lues")
    public ResponseEntity<Map<String, Long>> countUnread(final Authentication authentication) {
        final long count = notificationService.countUnread(authentication.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<Void> markAsRead(
            @PathVariable final String id,
            final Authentication authentication
    ) {
        notificationService.markAsRead(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<Void> markAllAsRead(final Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une notification (soft delete)")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable final String id,
            final Authentication authentication
    ) {
        notificationService.deleteNotification(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
