package com.kfokam.user_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfokam.user_api.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Point d'entrée d'authentification JWT.
 *
 * <p>Invoqué automatiquement par Spring Security lorsqu'un utilisateur
 * tente d'accéder à une ressource protégée <strong>sans token valide</strong>.</p>
 *
 * <p>Retourne une réponse JSON 401 Unauthorized au lieu de la page
 * de login HTML par défaut de Spring Security.</p>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Appelé quand une requête non authentifiée tente d'accéder à une route protégée.
     *
     * @param request       la requête HTTP
     * @param response      la réponse HTTP à remplir
     * @param authException l'exception d'authentification levée
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        log.warn("Accès non autorisé à '{}' : {}", request.getRequestURI(), authException.getMessage());

        // Configurer la réponse HTTP
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Corps de la réponse JSON
        ApiResponse<Void> body = ApiResponse.error(
                "Accès refusé : token JWT manquant ou invalide. " +
                "Veuillez vous connecter via POST /api/v1/auth/login"
        );

        // Sérialiser en JSON et écrire dans la réponse
        objectMapper.findAndRegisterModules(); // Support LocalDateTime
        objectMapper.writeValue(response.getWriter(), body);
    }
}
