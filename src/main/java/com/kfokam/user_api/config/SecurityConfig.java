package com.kfokam.user_api.config;

import com.kfokam.user_api.security.JwtAuthEntryPoint;
import com.kfokam.user_api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration principale de Spring Security.
 *
 * <p>Cette classe configure :</p>
 * <ul>
 *   <li><strong>Routes publiques</strong> : inscription, login, Swagger, H2</li>
 *   <li><strong>Routes protégées</strong> : tout le reste nécessite un JWT valide</li>
 *   <li><strong>Session stateless</strong> : pas de session HTTP (REST pur)</li>
 *   <li><strong>BCrypt</strong> : encodage des mots de passe</li>
 *   <li><strong>Filtre JWT</strong> : injecté avant le filtre d'auth classique</li>
 * </ul>
 *
 * <p><strong>Note architecture :</strong> {@code UserDetailsService} est défini dans
 * {@code UserDetailsServiceImpl} (package security) et non ici, afin d'éviter
 * la dépendance circulaire :</p>
 * <pre>
 *   SecurityConfig -> JwtAuthenticationFilter -> UserDetailsService -> SecurityConfig  (CYCLE)
 *   SecurityConfig -> JwtAuthenticationFilter -> UserDetailsServiceImpl               (OK)
 * </pre>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    // UserRepository supprimé — UserDetailsService est maintenant dans UserDetailsServiceImpl

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthEntryPoint)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers(
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html"
                    ).permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()));

        return http.build();
    }

    /**
     * Fournisseur d'authentification qui utilise la base de données.
     *
     * <p>{@link UserDetailsService} est injecté en paramètre de méthode (et non défini
     * comme bean local ici) pour que Spring résolve {@code UserDetailsServiceImpl}
     * sans créer de cycle.</p>
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
