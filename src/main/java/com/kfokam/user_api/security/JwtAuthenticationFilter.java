package com.kfokam.user_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT qui s'exécute UNE FOIS par requête HTTP.
 *
 * <p>Ce filtre est au cœur de l'authentification par token.
 * Il intercepte chaque requête entrante et :</p>
 *
 * <ol>
 *   <li>Extrait le token JWT depuis l'en-tête {@code Authorization}</li>
 *   <li>Valide le token avec {@link JwtService}</li>
 *   <li>Charge l'utilisateur depuis la base de données</li>
 *   <li>Injecte l'authentification dans le contexte Spring Security</li>
 * </ol>
 *
 * <p>Format de l'en-tête attendu :</p>
 * <pre>
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSJ9...
 * </pre>
 *
 * <p>Étend {@link OncePerRequestFilter} pour garantir l'exécution unique.</p>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Logique principale du filtre : extraction et validation du JWT.
     *
     * @param request     la requête HTTP entrante
     * @param response    la réponse HTTP
     * @param filterChain la chaîne de filtres suivants
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lire l'en-tête Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Si l'en-tête est absent ou ne commence pas par "Bearer ", passer au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraire le token (supprimer le préfixe "Bearer ")
        final String jwt = authHeader.substring(7);

        // 4. Extraire l'email depuis le token
        final String email;
        try {
            email = jwtService.extraireEmail(jwt);
        } catch (Exception e) {
            log.warn("Impossible d'extraire l'email du token JWT : {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Si l'email est valide ET qu'il n'y a pas encore d'authentification en cours
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Charger l'utilisateur depuis la base de données
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 7. Valider le token
            if (jwtService.estTokenValide(jwt, userDetails)) {

                // 8. Créer l'objet d'authentification Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                           // credentials (null car déjà authentifié)
                                userDetails.getAuthorities()    // rôles/permissions
                        );

                // 9. Ajouter les détails de la requête Web
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 10. Enregistrer l'authentification dans le contexte Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Utilisateur '{}' authentifié via JWT", email);
            }
        }

        // 11. Passer au filtre suivant dans la chaîne
        filterChain.doFilter(request, response);
    }
}
