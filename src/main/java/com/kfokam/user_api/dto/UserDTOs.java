package com.kfokam.user_api.dto;

import com.kfokam.user_api.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// =====================================================================
//  FICHIER REGROUPANT TOUS LES DTOs DE L'API UTILISATEUR
//  (classes statiques imbriquées pour la lisibilité)
// =====================================================================

/**
 * Classe conteneur regroupant tous les DTOs de l'API Utilisateur.
 *
 * <p>DTOs disponibles :</p>
 * <ul>
 *   <li>{@link RegisterRequest}  - Inscription d'un nouvel utilisateur</li>
 *   <li>{@link LoginRequest}     - Connexion / authentification</li>
 *   <li>{@link AuthResponse}     - Réponse avec le token JWT</li>
 *   <li>{@link UserResponse}     - Données d'un utilisateur (sans mot de passe)</li>
 *   <li>{@link UpdateRequest}    - Mise à jour d'un utilisateur</li>
 * </ul>
 *
 * @author KFOKAM48
 * @version 1.0
 */
public class UserDTOs {

    // Empêche l'instanciation de cette classe conteneur
    private UserDTOs() {}

    // =====================================================================

    /**
     * DTO pour l'inscription d'un nouvel utilisateur.
     * Utilisé pour {@code POST /api/v1/auth/register}
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Données requises pour créer un compte utilisateur")
    public static class RegisterRequest {

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom complet", example = "Jean Dupont", requiredMode = Schema.RequiredMode.REQUIRED)
        private String nom;

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        @Schema(description = "Adresse email (identifiant unique)", example = "jean.dupont@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        @Schema(description = "Mot de passe (min. 6 caractères)", example = "monMotDePasse123", requiredMode = Schema.RequiredMode.REQUIRED)
        private String motDePasse;
    }

    // =====================================================================

    /**
     * DTO pour la connexion / authentification.
     * Utilisé pour {@code POST /api/v1/auth/login}
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Identifiants de connexion")
    public static class LoginRequest {

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        @Schema(description = "Adresse email", example = "jean.dupont@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Schema(description = "Mot de passe", example = "monMotDePasse123", requiredMode = Schema.RequiredMode.REQUIRED)
        private String motDePasse;
    }

    // =====================================================================

    /**
     * DTO retourné après une authentification réussie.
     * Contient le token JWT et les infos de l'utilisateur connecté.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Réponse d'authentification avec token JWT")
    public static class AuthResponse {

        @Schema(description = "Token JWT à inclure dans les requêtes suivantes", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;

        @Schema(description = "Type de token (toujours Bearer)", example = "Bearer")
        @Builder.Default
        private String tokenType = "Bearer";

        @Schema(description = "Durée de validité en secondes", example = "86400")
        private long expiresIn;

        @Schema(description = "Email de l'utilisateur connecté", example = "jean.dupont@email.com")
        private String email;

        @Schema(description = "Nom de l'utilisateur connecté", example = "Jean Dupont")
        private String nom;

        @Schema(description = "Rôle de l'utilisateur", example = "ROLE_USER")
        private String role;
    }

    // =====================================================================

    /**
     * DTO représentant un utilisateur dans les réponses API.
     * Ne contient JAMAIS le mot de passe.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Données d'un utilisateur (sans mot de passe)")
    public static class UserResponse {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Nom complet", example = "Jean Dupont")
        private String nom;

        @Schema(description = "Adresse email", example = "jean.dupont@email.com")
        private String email;

        @Schema(description = "Rôle", example = "ROLE_USER")
        private Role role;

        @Schema(description = "Compte actif", example = "true")
        private boolean actif;

        @Schema(description = "Date de création", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "Date de mise à jour", example = "2024-01-15T14:00:00")
        private LocalDateTime updatedAt;
    }

    // =====================================================================

    /**
     * DTO pour la mise à jour d'un utilisateur.
     * Tous les champs sont optionnels (mise à jour partielle).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Données modifiables d'un utilisateur")
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nouveau nom (optionnel)", example = "Jean-Pierre Dupont")
        private String nom;

        @Email(message = "Format d'email invalide")
        @Schema(description = "Nouvel email (optionnel)", example = "jeanpierre@email.com")
        private String email;

        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        @Schema(description = "Nouveau mot de passe (optionnel, min. 6 caractères)", example = "nouveauMotDePasse456")
        private String motDePasse;
    }
}
