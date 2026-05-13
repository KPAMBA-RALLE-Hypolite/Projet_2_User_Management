package com.kfokam.user_api.security;

import com.kfokam.user_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implémentation de {@link UserDetailsService} qui charge les utilisateurs
 * depuis la base de données par leur email.
 *
 * <p>Extrait de {@code SecurityConfig} pour éviter une dépendance circulaire :</p>
 * <pre>
 *   SecurityConfig → JwtAuthenticationFilter → UserDetailsService → SecurityConfig  ✗
 *   SecurityConfig → JwtAuthenticationFilter → UserDetailsServiceImpl               ✓
 * </pre>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur trouvé avec l'email : " + email
                ));
    }
}
