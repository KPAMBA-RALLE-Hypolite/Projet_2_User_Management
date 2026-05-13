package com.kfokam.user_api.service;

import com.kfokam.user_api.dto.UserDTOs.AuthResponse;
import com.kfokam.user_api.dto.UserDTOs.LoginRequest;
import com.kfokam.user_api.dto.UserDTOs.RegisterRequest;
import com.kfokam.user_api.exception.EmailAlreadyExistsException;
import com.kfokam.user_api.model.Role;
import com.kfokam.user_api.model.User;
import com.kfokam.user_api.repository.UserRepository;
import com.kfokam.user_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service gérant l'inscription et la connexion des utilisateurs.
 *
 * <p>Flux d'inscription :</p>
 * <ol>
 *   <li>Vérifier que l'email n'existe pas déjà</li>
 *   <li>Hasher le mot de passe avec BCrypt</li>
 *   <li>Sauvegarder l'utilisateur en BDD</li>
 *   <li>Générer un token JWT</li>
 *   <li>Retourner le token + infos utilisateur</li>
 * </ol>
 *
 * <p>Flux de connexion :</p>
 * <ol>
 *   <li>Déléguer la vérification à Spring Security (AuthenticationManager)</li>
 *   <li>Spring Security charge l'utilisateur et vérifie le mot de passe BCrypt</li>
 *   <li>Générer un nouveau token JWT</li>
 *   <li>Retourner le token + infos utilisateur</li>
 * </ol>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Inscrit un nouvel utilisateur et retourne un token JWT.
     *
     * @param request les données d'inscription (nom, email, mot de passe)
     * @return la réponse d'authentification avec le token JWT
     * @throws EmailAlreadyExistsException si l'email est déjà utilisé
     */
    @Transactional
    public AuthResponse inscrire(RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email : {}", request.getEmail());

        // 1. Vérifier l'unicité de l'email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Construire l'entité utilisateur avec le mot de passe hashé
        User user = User.builder()
                .nom(request.getNom().trim())
                .email(request.getEmail().toLowerCase().trim())
                // Hasher le mot de passe avec BCrypt (jamais stocker en clair !)
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(Role.ROLE_USER) // Par défaut : utilisateur standard
                .actif(true)
                .build();

        // 3. Sauvegarder en base de données
        User savedUser = userRepository.save(user);
        log.info("Utilisateur inscrit avec succès. ID : {}", savedUser.getId());

        // 4. Générer le token JWT avec des claims supplémentaires (rôle)
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", savedUser.getRole().name());
        claims.put("nom", savedUser.getNom());
        String token = jwtService.genererToken(claims, savedUser);

        // 5. Construire et retourner la réponse
        return buildAuthResponse(token, savedUser);
    }

    /**
     * Connecte un utilisateur existant et retourne un token JWT.
     *
     * <p>Délègue la vérification des identifiants à Spring Security
     * via l'{@link AuthenticationManager}, qui utilise le
     * {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
     * configuré dans {@link com.kfokam.userapi.config.SecurityConfig}.</p>
     *
     * @param request les identifiants (email, mot de passe)
     * @return la réponse d'authentification avec le token JWT
     * @throws org.springframework.security.authentication.BadCredentialsException si les identifiants sont invalides
     */
    public AuthResponse connecter(LoginRequest request) {
        log.info("Tentative de connexion pour l'email : {}", request.getEmail());

        // 1. Spring Security vérifie email + mot de passe BCrypt
        //    Lance BadCredentialsException si incorrect
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getMotDePasse()
                )
        );

        // 2. Charger l'utilisateur depuis la BDD (authentification réussie ici)
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(); // Ne peut pas arriver si l'auth a réussi

        log.info("Connexion réussie pour l'utilisateur ID : {}", user.getId());

        // 3. Générer un nouveau token JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("nom", user.getNom());
        String token = jwtService.genererToken(claims, user);

        // 4. Retourner la réponse
        return buildAuthResponse(token, user);
    }

    /**
     * Construit la réponse d'authentification à partir du token et de l'utilisateur.
     *
     * @param token le token JWT généré
     * @param user  l'utilisateur authentifié
     * @return la réponse d'authentification
     */
    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationEnSecondes())
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole().name())
                .build();
    }
}
