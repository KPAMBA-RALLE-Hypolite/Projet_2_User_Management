package com.kfokam.user_api.config;

import com.kfokam.user_api.model.Role;
import com.kfokam.user_api.model.User;
import com.kfokam.user_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initialisation des données de démonstration au démarrage.
 *
 * <p>Crée automatiquement :</p>
 * <ul>
 *   <li>1 compte <strong>admin</strong> avec le rôle ROLE_ADMIN</li>
 *   <li>2 comptes <strong>utilisateurs</strong> avec le rôle ROLE_USER</li>
 * </ul>
 *
 * <p>Ces comptes permettent de tester l'API immédiatement.</p>
 *
 * <p><strong>Identifiants de test :</strong></p>
 * <pre>
 *   Admin : admin@kfokam.dev / Admin@1234
 *   User1 : alice@email.com  / Password@1
 *   User2 : bob@email.com    / Password@2
 * </pre>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Insère les utilisateurs de démonstration si la base est vide.
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Initialisation des utilisateurs de démonstration...");

                // Compte administrateur
                userRepository.save(User.builder()
                        .nom("Administrateur KFOKAM48")
                        .email("admin@kfokam.dev")
                        .motDePasse(passwordEncoder.encode("Admin@1234"))
                        .role(Role.ROLE_ADMIN)
                        .actif(true)
                        .build());

                // Utilisateurs standards
                userRepository.save(User.builder()
                        .nom("Alice Martin")
                        .email("alice@email.com")
                        .motDePasse(passwordEncoder.encode("Password@1"))
                        .role(Role.ROLE_USER)
                        .actif(true)
                        .build());

                userRepository.save(User.builder()
                        .nom("Bob Nguyen")
                        .email("bob@email.com")
                        .motDePasse(passwordEncoder.encode("Password@2"))
                        .role(Role.ROLE_USER)
                        .actif(true)
                        .build());

                log.info("3 utilisateurs de démonstration créés.");
                log.info("Admin  → admin@kfokam.dev / Admin@1234");
                log.info("User 1 → alice@email.com  / Password@1");
                log.info("User 2 → bob@email.com    / Password@2");
            }
        };
    }
}
