package com.kfokam.user_api.controller;

import com.kfokam.user_api.dto.ApiResponse;
import com.kfokam.user_api.dto.UserDTOs.UpdateRequest;
import com.kfokam.user_api.dto.UserDTOs.UserResponse;
import com.kfokam.user_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des utilisateurs (CRUD).
 *
 * <p>Tous les endpoints sont <strong>protégés</strong> : un token JWT valide
 * est requis dans l'en-tête {@code Authorization: Bearer <token>}.</p>
 *
 * <p>Endpoints :</p>
 * <ul>
 *   <li>{@code GET    /api/v1/users}       — Lister tous les utilisateurs (AUTH)</li>
 *   <li>{@code GET    /api/v1/users/{id}}  — Voir un utilisateur (AUTH)</li>
 *   <li>{@code PUT    /api/v1/users/{id}}  — Modifier un utilisateur (AUTH)</li>
 *   <li>{@code DELETE /api/v1/users/{id}}  — Supprimer un utilisateur (ADMIN)</li>
 * </ul>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Gestion des Utilisateurs", description = "CRUD utilisateurs — Token JWT requis")
@SecurityRequirement(name = "bearerAuth")  // Indique à Swagger que ces endpoints nécessitent un JWT
public class UserController {

    private final UserService userService;

    // ============================================================
    //  GET /api/v1/users — Lister tous les utilisateurs
    // ============================================================

    /**
     * Retourne la liste de tous les utilisateurs.
     * Accessible à tout utilisateur authentifié.
     *
     * @return 200 OK avec la liste des utilisateurs
     */
    @GetMapping
    @Operation(
        summary     = "Lister les utilisateurs",
        description = "Retourne la liste de tous les utilisateurs. **Token JWT requis.**"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste retournée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token manquant ou invalide")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getTousUtilisateurs() {
        log.info("GET /api/v1/users");
        List<UserResponse> users = userService.getTousUtilisateurs();
        return ResponseEntity.ok(ApiResponse.success(
                users.size() + " utilisateur(s) trouvé(s)", users
        ));
    }

    // ============================================================
    //  GET /api/v1/users/{id} — Récupérer un utilisateur
    // ============================================================

    /**
     * Récupère les détails d'un utilisateur par son ID.
     *
     * @param id l'identifiant de l'utilisateur
     * @return 200 OK avec l'utilisateur, ou 404 si non trouvé
     */
    @GetMapping("/{id}")
    @Operation(
        summary     = "Récupérer un utilisateur",
        description = "Retourne les détails d'un utilisateur par son ID. **Token JWT requis.**"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token manquant ou invalide"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUtilisateurParId(
            @Parameter(description = "ID de l'utilisateur", example = "1", required = true)
            @PathVariable Long id) {

        log.info("GET /api/v1/users/{}", id);
        UserResponse user = userService.getUtilisateurParId(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur trouvé", user));
    }

    // ============================================================
    //  PUT /api/v1/users/{id} — Mettre à jour un utilisateur
    // ============================================================

    /**
     * Met à jour les informations d'un utilisateur.
     * Les champs non fournis ne sont pas modifiés.
     *
     * @param id      l'identifiant de l'utilisateur à modifier
     * @param request les nouvelles données
     * @return 200 OK avec l'utilisateur mis à jour
     */
    @PutMapping("/{id}")
    @Operation(
        summary     = "Mettre à jour un utilisateur",
        description = "Modifie nom, email et/ou mot de passe. Champs optionnels. **Token JWT requis.**"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Utilisateur mis à jour"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token manquant ou invalide"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    public ResponseEntity<ApiResponse<UserResponse>> mettreAJour(
            @Parameter(description = "ID de l'utilisateur", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest request) {

        log.info("PUT /api/v1/users/{}", id);
        UserResponse user = userService.mettreAJourUtilisateur(id, request);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur mis à jour avec succès", user));
    }

    // ============================================================
    //  DELETE /api/v1/users/{id} — Supprimer un utilisateur
    // ============================================================

    /**
     * Supprime définitivement un utilisateur.
     *
     * <p><strong>Accès restreint :</strong> réservé aux utilisateurs avec le rôle ADMIN.
     * {@code @PreAuthorize} ajoute une vérification supplémentaire au niveau méthode.</p>
     *
     * @param id l'identifiant de l'utilisateur à supprimer
     * @return 200 OK avec confirmation
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")  // Double sécurité : aussi dans SecurityConfig
    @Operation(
        summary     = "Supprimer un utilisateur",
        description = "Supprime définitivement un utilisateur. **Réservé aux ADMIN. Token JWT requis.**"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Utilisateur supprimé"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token manquant ou invalide"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès refusé — rôle ADMIN requis"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @Parameter(description = "ID de l'utilisateur", example = "1", required = true)
            @PathVariable Long id) {

        log.info("DELETE /api/v1/users/{}", id);
        userService.supprimerUtilisateur(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé avec succès"));
    }
}
