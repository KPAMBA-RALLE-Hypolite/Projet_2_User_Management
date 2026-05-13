package com.kfokam.user_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wrapper générique standardisant toutes les réponses de l'API.
 *
 * <p>Exemple de réponse succès :</p>
 * <pre>
 * { "success": true, "message": "...", "data": {...}, "timestamp": "..." }
 * </pre>
 *
 * <p>Exemple de réponse erreur :</p>
 * <pre>
 * { "success": false, "message": "Email déjà utilisé", "timestamp": "..." }
 * </pre>
 *
 * @param <T> type de la donnée encapsulée
 * @author KFOKAM48
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Réponse de succès avec données. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data)
                .timestamp(LocalDateTime.now()).build();
    }

    /** Réponse de succès sans données. */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true).message(message)
                .timestamp(LocalDateTime.now()).build();
    }

    /** Réponse d'erreur. */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false).message(message)
                .timestamp(LocalDateTime.now()).build();
    }
}
