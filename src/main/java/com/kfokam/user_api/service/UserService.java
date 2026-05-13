package com.kfokam.user_api.service;

import com.kfokam.user_api.dto.UserDTOs.UpdateRequest;
import com.kfokam.user_api.dto.UserDTOs.UserResponse;
import com.kfokam.user_api.exception.EmailAlreadyExistsException;
import com.kfokam.user_api.exception.UserNotFoundException;
import com.kfokam.user_api.model.User;
import com.kfokam.user_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des utilisateurs (CRUD).
 *
 * <p>Opérations disponibles :</p>
 * <ul>
 *   <li>Lister tous les utilisateurs</li>
 *   <li>Récupérer un utilisateur par ID</li>
 *   <li>Mettre à jour un utilisateur (nom, email, mot de passe)</li>
 *   <li>Supprimer un utilisateur</li>
 * </ul>
 *
 * <p><strong>Sécurité :</strong> Le mot de passe n'est jamais retourné
 * dans les réponses (le DTO {@link UserResponse} ne contient pas ce champ).</p>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // =====================================================
    //  LIRE
    // =====================================================

    /**
     * Retourne la liste de tous les utilisateurs.
     *
     * @return liste des utilisateurs (sans mots de passe)
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getTousUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        return userRepository.findAll()
                .stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id l'identifiant de l'utilisateur
     * @return les données de l'utilisateur (sans mot de passe)
     * @throws UserNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public UserResponse getUtilisateurParId(Long id) {
        log.info("Récupération de l'utilisateur ID : {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return convertirEnDTO(user);
    }

    // =====================================================
    //  METTRE À JOUR
    // =====================================================

    /**
     * Met à jour les informations d'un utilisateur.
     *
     * <p>Seuls les champs fournis (non null) sont mis à jour.
     * Le nouveau mot de passe est automatiquement hashé avec BCrypt.</p>
     *
     * @param id      l'identifiant de l'utilisateur à modifier
     * @param request les nouvelles données (champs optionnels)
     * @return les données de l'utilisateur mis à jour
     * @throws UserNotFoundException       si l'utilisateur n'existe pas
     * @throws EmailAlreadyExistsException si le nouvel email est déjà pris
     */
    @Transactional
    public UserResponse mettreAJourUtilisateur(Long id, UpdateRequest request) {
        log.info("Mise à jour de l'utilisateur ID : {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Mise à jour partielle : seulement les champs fournis
        if (request.getNom() != null && !request.getNom().isBlank()) {
            user.setNom(request.getNom().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String nouvelEmail = request.getEmail().toLowerCase().trim();
            // Vérifier que le nouvel email n'est pas déjà pris par un autre utilisateur
            if (!nouvelEmail.equals(user.getEmail()) && userRepository.existsByEmail(nouvelEmail)) {
                throw new EmailAlreadyExistsException(nouvelEmail);
            }
            user.setEmail(nouvelEmail);
        }

        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            // Hasher le nouveau mot de passe avant de le sauvegarder
            user.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        }

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur ID {} mis à jour avec succès", id);

        return convertirEnDTO(updatedUser);
    }

    // =====================================================
    //  SUPPRIMER
    // =====================================================

    /**
     * Supprime définitivement un utilisateur.
     *
     * <p><strong>Accès restreint ADMIN</strong> (configuré dans SecurityConfig).</p>
     *
     * @param id l'identifiant de l'utilisateur à supprimer
     * @throws UserNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public void supprimerUtilisateur(Long id) {
        log.info("Suppression de l'utilisateur ID : {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
        log.info("Utilisateur ID {} supprimé avec succès", id);
    }

    // =====================================================
    //  UTILITAIRE PRIVÉ
    // =====================================================

    /**
     * Convertit une entité {@link User} en {@link UserResponse} DTO.
     *
     * <p><strong>Important :</strong> le mot de passe n'est JAMAIS inclus
     * dans le DTO de réponse.</p>
     *
     * @param user l'entité à convertir
     * @return le DTO sans mot de passe
     */
    private UserResponse convertirEnDTO(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .email(user.getEmail())
                .role(user.getRole())
                .actif(user.isActif())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
