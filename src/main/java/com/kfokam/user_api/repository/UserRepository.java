package com.kfokam.user_api.repository;

import com.kfokam.user_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA pour la gestion des utilisateurs en base de données.
 *
 * <p>Hérite de {@link JpaRepository} qui fournit automatiquement :</p>
 * <ul>
 *   <li>save(), findById(), findAll(), deleteById(), count(), etc.</li>
 * </ul>
 *
 * <p>Spring Data JPA génère l'implémentation SQL à partir du nom des méthodes.</p>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son email.
     *
     * <p>Utilisé par Spring Security lors de l'authentification pour
     * charger les détails de l'utilisateur.</p>
     *
     * <p>SQL généré : {@code SELECT * FROM users WHERE email = ?}</p>
     *
     * @param email l'adresse email à rechercher
     * @return un Optional contenant l'utilisateur, ou vide si non trouvé
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email est déjà utilisé.
     *
     * <p>Utilisé lors de l'inscription pour éviter les doublons.</p>
     *
     * @param email l'email à vérifier
     * @return true si l'email est déjà enregistré
     */
    boolean existsByEmail(String email);
}
