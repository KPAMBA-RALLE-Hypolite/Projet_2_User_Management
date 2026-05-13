package com.kfokam.user_api.exception;

/**
 * Exception levée quand un utilisateur est introuvable en BDD.
 * → HTTP 404 Not Found
 *
 * @author KFOKAM48
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Utilisateur non trouvé avec l'ID : " + id);
    }
    public UserNotFoundException(String email) {
        super("Utilisateur non trouvé avec l'email : " + email);
    }
}
