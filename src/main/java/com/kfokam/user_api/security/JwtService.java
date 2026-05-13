package com.kfokam.user_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsable de la gestion des tokens JWT (JSON Web Token).
 *
 * <p>Un token JWT est composé de 3 parties séparées par des points :</p>
 * <pre>
 *   HEADER.PAYLOAD.SIGNATURE
 *   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSJ9.abc123
 * </pre>
 *
 * <p>Fonctions assurées :</p>
 * <ul>
 *   <li>Génération d'un token à partir des données utilisateur</li>
 *   <li>Extraction des informations (email, expiration) d'un token</li>
 *   <li>Validation d'un token (signature + expiration)</li>
 * </ul>
 *
 * <p>L'algorithme utilisé est <strong>HMAC-SHA256 (HS256)</strong>.</p>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@Service
public class JwtService {

    /**
     * Clé secrète lue depuis application.properties.
     * Utilisée pour signer et vérifier les tokens.
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Durée de validité du token en millisecondes.
     * Par défaut : 86400000 ms = 24 heures.
     */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // =====================================================
    //  GÉNÉRATION DU TOKEN
    // =====================================================

    /**
     * Génère un token JWT pour un utilisateur.
     *
     * @param userDetails les détails de l'utilisateur Spring Security
     * @return le token JWT signé
     */
    public String genererToken(UserDetails userDetails) {
        return genererToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un token JWT avec des claims (données) supplémentaires.
     *
     * @param claimsSupplementaires données additionnelles à inclure dans le token
     * @param userDetails           les détails de l'utilisateur
     * @return le token JWT signé
     */
    public String genererToken(Map<String, Object> claimsSupplementaires, UserDetails userDetails) {
        return Jwts.builder()
                // Claims supplémentaires (ex: rôle)
                .setClaims(claimsSupplementaires)
                // Subject = email de l'utilisateur (identifiant unique)
                .setSubject(userDetails.getUsername())
                // Date d'émission du token
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Date d'expiration = maintenant + durée de validité
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                // Signature avec la clé secrète (algorithme HS256)
                .signWith(getCleSignature(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =====================================================
    //  EXTRACTION DES INFORMATIONS
    // =====================================================

    /**
     * Extrait l'email (subject) d'un token JWT.
     *
     * @param token le token JWT
     * @return l'email de l'utilisateur
     */
    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    /**
     * Extrait la date d'expiration d'un token.
     *
     * @param token le token JWT
     * @return la date d'expiration
     */
    public Date extraireExpiration(String token) {
        return extraireClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait un claim (donnée) spécifique d'un token.
     *
     * @param token          le token JWT
     * @param claimsResolver fonction d'extraction du claim
     * @param <T>            type du claim
     * @return la valeur du claim
     */
    public <T> T extraireClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraireTousClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse le token et extrait tous les claims.
     *
     * @param token le token JWT
     * @return les claims du token
     */
    private Claims extraireTousClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getCleSignature())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // =====================================================
    //  VALIDATION DU TOKEN
    // =====================================================

    /**
     * Vérifie si un token JWT est valide pour un utilisateur donné.
     *
     * <p>Un token est valide si :</p>
     * <ul>
     *   <li>Le subject (email) correspond à l'utilisateur</li>
     *   <li>Le token n'est pas expiré</li>
     *   <li>La signature est correcte</li>
     * </ul>
     *
     * @param token       le token JWT à valider
     * @param userDetails les détails de l'utilisateur
     * @return true si le token est valide
     */
    public boolean estTokenValide(String token, UserDetails userDetails) {
        try {
            final String email = extraireEmail(token);
            return email.equals(userDetails.getUsername()) && !estTokenExpire(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expiré");
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformé");
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT non supporté");
            return false;
        } catch (Exception e) {
            log.warn("Erreur de validation JWT : {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un token est expiré.
     *
     * @param token le token JWT
     * @return true si le token est expiré
     */
    private boolean estTokenExpire(String token) {
        return extraireExpiration(token).before(new Date());
    }

    /**
     * Retourne la durée de validité en secondes (pour la réponse auth).
     *
     * @return durée en secondes
     */
    public long getExpirationEnSecondes() {
        return jwtExpiration / 1000;
    }

    /**
     * Construit la clé de signature cryptographique à partir du secret.
     *
     * <p>Utilise {@link Keys#hmacShaKeyFor} pour créer une clé HMAC-SHA
     * à partir des octets du secret.</p>
     *
     * @return la clé de signature
     */
    private Key getCleSignature() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
