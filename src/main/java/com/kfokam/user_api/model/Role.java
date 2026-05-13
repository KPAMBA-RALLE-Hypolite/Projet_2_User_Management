package com.kfokam.user_api.model;

/**
 * Enumération des rôles disponibles dans l'application.
 *
 * <p>Spring Security utilise ces rôles pour contrôler l'accès
 * aux différentes routes de l'API.</p>
 *
 * <p>Convention Spring Security : les rôles sont préfixés par {@code ROLE_}
 * pour être reconnus par les règles d'autorisation.</p>
 *
 * @author KFOKAM48
 * @version 1.0
 */
public enum Role {

    /**
     * Utilisateur standard : accès limité à ses propres ressources.
     */
    ROLE_USER,

    /**
     * Administrateur : accès complet à toutes les ressources.
     */
    ROLE_ADMIN
}
