package com.kfokam.user_api.exception;

/**
 * Exception levée quand un email est déjà utilisé.
 * → HTTP 409 Conflict
 *
 * @author KFOKAM48
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("L'email '" + email + "' est déjà utilisé par un autre compte.");
    }
}
