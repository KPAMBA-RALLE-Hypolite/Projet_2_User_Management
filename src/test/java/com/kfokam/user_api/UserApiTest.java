package com.kfokam.user_api;

import com.kfokam.user_api.repository.UserRepository;
import com.kfokam.user_api.dto.UserDTOs.*;
import com.kfokam.user_api.exception.EmailAlreadyExistsException;
import com.kfokam.user_api.exception.UserNotFoundException;
import com.kfokam.user_api.model.Role;
import com.kfokam.user_api.model.User;
import com.kfokam.user_api.security.JwtService;
import com.kfokam.user_api.service.AuthService;
import com.kfokam.user_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AuthService} et {@link UserService}.
 *
 * @author KFOKAM48
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires — User API")
class UserApiTest {

    // =====================================================
    //  TESTS AUTHSERVICE
    // =====================================================

    @Nested
    @DisplayName("AuthService")
    class AuthServiceTests {

        @Mock private UserRepository userRepository;
        @Mock private PasswordEncoder passwordEncoder;
        @Mock private JwtService jwtService;
        @Mock private AuthenticationManager authenticationManager;

        @InjectMocks private AuthService authService;

        private User userExemple;

        @BeforeEach
        void setUp() {
            userExemple = User.builder()
                    .id(1L).nom("Jean Dupont").email("jean@email.com")
                    .motDePasse("hashedPassword").role(Role.ROLE_USER)
                    .actif(true).createdAt(LocalDateTime.now()).build();
        }

        @Test
        @DisplayName("inscrire() → doit créer l'utilisateur et retourner un token")
        void inscrire_doitRetournerToken() {
            // GIVEN
            RegisterRequest request = new RegisterRequest("Jean Dupont", "jean@email.com", "password123");
            when(userRepository.existsByEmail("jean@email.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(userExemple);
            when(jwtService.genererToken(anyMap(), any())).thenReturn("token.jwt.123");
            when(jwtService.getExpirationEnSecondes()).thenReturn(86400L);

            // WHEN
            AuthResponse result = authService.inscrire(request);

            // THEN
            assertThat(result.getToken()).isEqualTo("token.jwt.123");
            assertThat(result.getEmail()).isEqualTo("jean@email.com");
            assertThat(result.getNom()).isEqualTo("Jean Dupont");
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("inscrire() avec email existant → doit lever EmailAlreadyExistsException")
        void inscrire_emailExistant_doitLeverException() {
            // GIVEN
            RegisterRequest request = new RegisterRequest("Jean", "jean@email.com", "pass");
            when(userRepository.existsByEmail("jean@email.com")).thenReturn(true);

            // WHEN / THEN
            assertThatThrownBy(() -> authService.inscrire(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("jean@email.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("connecter() avec bons identifiants → doit retourner un token")
        void connecter_bonsIdentifiants_doitRetournerToken() {
            // GIVEN
            LoginRequest request = new LoginRequest("jean@email.com", "password123");
            when(authenticationManager.authenticate(any())).thenReturn(
                    new UsernamePasswordAuthenticationToken("jean@email.com", null)
            );
            when(userRepository.findByEmail("jean@email.com")).thenReturn(Optional.of(userExemple));
            when(jwtService.genererToken(anyMap(), any())).thenReturn("token.jwt.456");
            when(jwtService.getExpirationEnSecondes()).thenReturn(86400L);

            // WHEN
            AuthResponse result = authService.connecter(request);

            // THEN
            assertThat(result.getToken()).isEqualTo("token.jwt.456");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("connecter() avec mauvais identifiants → doit lever BadCredentialsException")
        void connecter_mauvaisIdentifiants_doitLeverException() {
            // GIVEN
            LoginRequest request = new LoginRequest("jean@email.com", "mauvaisPass");
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // WHEN / THEN
            assertThatThrownBy(() -> authService.connecter(request))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    // =====================================================
    //  TESTS USERSERVICE
    // =====================================================

    @Nested
    @DisplayName("UserService")
    class UserServiceTests {

        @Mock private UserRepository userRepository;
        @Mock private PasswordEncoder passwordEncoder;

        @InjectMocks private UserService userService;

        private User userExemple;

        @BeforeEach
        void setUp() {
            userExemple = User.builder()
                    .id(1L).nom("Alice Martin").email("alice@email.com")
                    .motDePasse("hashedPassword").role(Role.ROLE_USER)
                    .actif(true).createdAt(LocalDateTime.now()).build();
        }

        @Test
        @DisplayName("getTousUtilisateurs() → doit retourner la liste")
        void getTousUtilisateurs_doitRetournerListe() {
            when(userRepository.findAll()).thenReturn(List.of(userExemple));

            List<UserResponse> result = userService.getTousUtilisateurs();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("alice@email.com");
            // Vérifier que le mot de passe n'est pas dans le DTO
            // (UserResponse n'a pas de champ motDePasse)
        }

        @Test
        @DisplayName("getUtilisateurParId() avec ID valide → doit retourner l'utilisateur")
        void getUtilisateurParId_idValide_doitRetourner() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(userExemple));

            UserResponse result = userService.getUtilisateurParId(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNom()).isEqualTo("Alice Martin");
        }

        @Test
        @DisplayName("getUtilisateurParId() avec ID inexistant → doit lever UserNotFoundException")
        void getUtilisateurParId_idInexistant_doitLeverException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUtilisateurParId(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("mettreAJourUtilisateur() → doit mettre à jour les champs fournis")
        void mettreAJourUtilisateur_doitMettreAJour() {
            UpdateRequest request = new UpdateRequest("Alice Dupont", null, null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(userExemple));
            when(userRepository.save(any(User.class))).thenReturn(userExemple);

            UserResponse result = userService.mettreAJourUtilisateur(1L, request);

            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("mettreAJour() avec nouveau mot de passe → doit le hasher")
        void mettreAJour_avecNouveauMotDePasse_doitHasher() {
            UpdateRequest request = new UpdateRequest(null, null, "nouveauPass123");
            when(userRepository.findById(1L)).thenReturn(Optional.of(userExemple));
            when(passwordEncoder.encode("nouveauPass123")).thenReturn("newHash");
            when(userRepository.save(any(User.class))).thenReturn(userExemple);

            userService.mettreAJourUtilisateur(1L, request);

            verify(passwordEncoder).encode("nouveauPass123");
        }

        @Test
        @DisplayName("supprimerUtilisateur() avec ID valide → doit supprimer")
        void supprimerUtilisateur_idValide_doitSupprimer() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            userService.supprimerUtilisateur(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("supprimerUtilisateur() avec ID inexistant → doit lever UserNotFoundException")
        void supprimerUtilisateur_idInexistant_doitLeverException() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> userService.supprimerUtilisateur(99L))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).deleteById(any());
        }
    }
}