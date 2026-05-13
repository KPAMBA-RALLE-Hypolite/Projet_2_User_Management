package com.kfokam.user_api.exception;

import com.kfokam.user_api.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global d'exceptions — intercepte toutes les erreurs
 * des contrôleurs REST et retourne des réponses HTTP cohérentes.
 *
 * <p>Mapping des exceptions :</p>
 * <ul>
 *   <li>{@link UserNotFoundException}          → 404 Not Found</li>
 *   <li>{@link EmailAlreadyExistsException}    → 409 Conflict</li>
 *   <li>{@link BadCredentialsException}        → 401 Unauthorized</li>
 *   <li>{@link DisabledException}              → 403 Forbidden</li>
 *   <li>{@link MethodArgumentNotValidException}→ 400 Bad Request</li>
 *   <li>{@link Exception}                      → 500 Internal Server Error</li>
 * </ul>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 — Utilisateur introuvable. */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("Utilisateur non trouvé : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /** 409 — Email déjà utilisé. */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailExists(EmailAlreadyExistsException ex) {
        log.warn("Email déjà existant : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 401 — Mauvais identifiants (email/mot de passe incorrect).
     * Levée par Spring Security lors d'une authentification échouée.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Tentative de connexion avec mauvais identifiants");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Email ou mot de passe incorrect."));
    }

    /**
     * 403 — Compte désactivé.
     * Levée par Spring Security quand isEnabled() retourne false.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
        log.warn("Tentative de connexion sur un compte désactivé");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Ce compte a été désactivé."));
    }

    /** 400 — Erreurs de validation des DTOs. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> erreurs = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erreurs.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Erreurs de validation : {}", erreurs);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Données invalides.")
                        .data(erreurs)
                        .build());
    }

    /** 500 — Erreur interne générique. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Erreur interne : {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Une erreur interne s'est produite."));
    }
}
