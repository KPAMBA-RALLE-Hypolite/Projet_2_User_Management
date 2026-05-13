package com.kfokam.user_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI (Swagger) avec support de l'authentification JWT.
 *
 * <p>Cette configuration ajoute dans Swagger UI :</p>
 * <ul>
 *   <li>Un bouton "Authorize" pour saisir le token Bearer JWT</li>
 *   <li>L'envoi automatique de l'en-tête {@code Authorization: Bearer <token>}</li>
 *   <li>Les informations du projet (titre, version, auteur)</li>
 * </ul>
 *
 * <p>URL Swagger : <a href="http://localhost:8080/swagger-ui.html">
 * http://localhost:8080/swagger-ui.html</a></p>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Configuration
public class SwaggerConfig {

    /** Nom du schéma de sécurité JWT (référencé dans les annotations Swagger). */
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Configure l'interface OpenAPI complète avec JWT.
     *
     * @return la configuration OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(buildApiInfo())
                .servers(List.of(buildLocalServer()))
                // Déclare le schéma de sécurité JWT Bearer
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildJwtSecurityScheme())
                )
                // Applique l'authentification JWT à tous les endpoints par défaut
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * Informations générales de l'API.
     */
    private Info buildApiInfo() {
        return new Info()
                .title("API de Gestion des Utilisateurs")
                .version("1.0.0")
                .description("""
                        ## Description
                        API REST sécurisée avec **Spring Security + JWT** pour gérer les utilisateurs.
                        
                        ## Authentification
                        1. **Créer un compte** → `POST /api/v1/auth/register`
                        2. **Se connecter** → `POST /api/v1/auth/login` (récupère le token JWT)
                        3. **Cliquer sur "Authorize"** → Saisir : `Bearer <votre_token>`
                        4. Toutes les requêtes suivantes incluront automatiquement le token.
                        
                        ## Endpoints publics (sans token)
                        - `POST /api/v1/auth/register` — Créer un compte
                        - `POST /api/v1/auth/login`    — Se connecter
                        
                        ## Endpoints protégés (token requis)
                        - `GET    /api/v1/users`       — Lister les utilisateurs
                        - `GET    /api/v1/users/{id}`  — Voir un utilisateur
                        - `PUT    /api/v1/users/{id}`  — Modifier un utilisateur
                        - `DELETE /api/v1/users/{id}`  — Supprimer (ADMIN seulement)
                        """)
                .contact(new Contact()
                        .name("KFOKAM48")
                        .email("contact@kfokam48.dev")
                        .url("https://github.com/kfokam48"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Serveur local pour le développement.
     */
    private Server buildLocalServer() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Serveur de développement local");
        return server;
    }

    /**
     * Schéma de sécurité JWT Bearer.
     *
     * <p>Configure Swagger UI pour afficher le bouton "Authorize"
     * et envoyer l'en-tête {@code Authorization: Bearer <token>}.</p>
     */
    private SecurityScheme buildJwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")           // Schéma HTTP Bearer
                .bearerFormat("JWT")        // Format du token
                .description("Saisir le token JWT obtenu lors du login. Format : Bearer <token>");
    }
}
