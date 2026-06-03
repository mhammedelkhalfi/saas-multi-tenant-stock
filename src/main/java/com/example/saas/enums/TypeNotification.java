package com.example.saas.enums;

public enum TypeNotification {
    STOCK_ALERT,        // seuil alert_threshold dépassé
    STOCK_MOVEMENT,     // IN / OUT
    TENANT_STATUS,      // approuvé, suspendu, etc.
    USER_STATUS,        // compte activé / désactivé
    SYSTEM
}
