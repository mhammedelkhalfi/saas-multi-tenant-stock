package com.example.saas.notification;

/**
 * Chemins STOMP utilisés pour envoyer les notifications en temps réel.
 * <p>
 * - USER_QUEUE : message privé pour un utilisateur (via convertAndSendToUser)
 * - tenantTopic : broadcast pour tous les clients abonnés au tenant
 */
public final class NotificationDestination {

    public static final String USER_QUEUE = "/queue/notifications";

    private NotificationDestination() {
    }

    public static String tenantTopic(final String tenantId) {
        return "/topic/tenant/" + tenantId + "/notifications";
    }
}
