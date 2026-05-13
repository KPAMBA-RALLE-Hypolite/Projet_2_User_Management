package com.kfokam.user_api.controller;

import com.kfokam.user_api.dto.ApiResponse;
import com.kfokam.user_api.dto.UserDTOs.AuthResponse;
import com.kfokam.user_api.dto.UserDTOs.LoginRequest;
import com.kfokam.user_api.dto.UserDTOs.RegisterRequest;
import com.kfokam.user_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour l'authentification.
 *
 * <p>Endpoints <strong>publics</strong> (pas de token requis) :</p>
 * <ul>
 *   <li>{@code POST /api/v1/auth/register} — Inscription d'un nouvel utilisateur</li>
 *   <li>{@code POST /api/v1/auth/login}    — Connexion et obtention du token JWT</li>
 * </ul>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints publics d'inscription et de connexion")
// Exclure ces endpoints du schéma de sécurité JWT dans Swagger (ils sont publics)
@SecurityRequirement(name = "")
public class AuthController {

    private final AuthService authService;

    // ============================================================
    //  POST /api/v1/auth/register — Inscription
    // ============================================================

    /**
     * Inscrit un nouvel utilisateur et retourne un token JWT.
     *
     * @param request les données d'inscription (nom, email, mot de passe)
     * @return 201 Created avec le token JWT et les infos utilisateur
     */
    @PostMapping("/register")
    @Operation(
        summary     = "Inscription",
        description = "Crée un nouveau compte utilisateur et retourne un token JWT. **Endpoint public.**"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Compte créé, token JWT retourné"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> inscrire(
            @Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/v1/auth/register — Email : {}", request.getEmail());
        AuthResponse response = authService.inscrire(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Compte créé avec succès. Bienvenue !", response));
    }

    // ============================================================
    //  POST /api/v1/auth/login — Connexion
    // ============================================================

    /**
     * Authentifie un utilisateur et retourne un token JWT valide 24h.
     *
     * <p>Le token JWT retourné doit être inclus dans toutes les requêtes
     * suivantes avec l'en-tête : {@code Authorization: Bearer <token>}</p>
     *
     * @param request les identifiants (email, mot de passe)
     * @return 200 OK avec le token JWT
     */
    @PostMapping("/login")
    @Operation(
        summary     = "Connexion",
        description = "Authentifie un utilisateur et retourne un token JWT (valide 24h). **Endpoint public.**\n\n" +
                      "**Utiliser le token reçu** : Cliquer sur 'Authorize' → saisir `Bearer <token>`"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connexion réussie, token JWT retourné"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Compte désactivé")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> connecter(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /api/v1/auth/login — Email : {}", request.getEmail());
        AuthResponse response = authService.connecter(request);

        return ResponseEntity.ok(ApiResponse.success(
                "Connexion réussie. Token valide 24h.", response
        ));
    }
}
