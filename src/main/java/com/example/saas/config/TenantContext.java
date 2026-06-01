package com.example.saas.config;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_SCHEMA = new ThreadLocal<>();


    /**
     * Definit l'identifiant du tenant pour le thread courant.
     */
    public static void setCurrentTenant(final String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    public static void setCurrentSchema(final String schema) {
        CURRENT_SCHEMA.set(schema);
    }

    /**
     * Recupere l'identifiant du tenant pour le thread courant.
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }


    public static String getCurrentSchema() {
        return CURRENT_SCHEMA.get();
    }

    /**
     * Nettoie le tenant du thread courant.
     * IMPORTANT: doit etre appele dans un bloc finally
     * pour eviter les fuites de memoire (memory leak)
     * et les fuites de donnees entre requetes HTTP.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_SCHEMA.remove();
    }
}
