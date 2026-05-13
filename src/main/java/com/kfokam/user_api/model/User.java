package com.kfokam.user_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entité JPA représentant un utilisateur en base de données.
 *
 * <p>Implémente {@link UserDetails} pour s'intégrer nativement avec
 * Spring Security. Cela permet à Spring Security de charger l'utilisateur
 * directement depuis la base de données pour l'authentification.</p>
 *
 * <p>Champs :</p>
 * <ul>
 *   <li>id          : identifiant auto-généré</li>
 *   <li>nom         : nom complet de l'utilisateur</li>
 *   <li>email       : adresse email unique (sert de username)</li>
 *   <li>motDePasse  : mot de passe hashé avec BCrypt</li>
 *   <li>role        : rôle (ROLE_USER ou ROLE_ADMIN)</li>
 *   <li>actif       : compte actif ou désactivé</li>
 *   <li>createdAt   : date de création automatique</li>
 *   <li>updatedAt   : date de mise à jour automatique</li>
 * </ul>
 *
 * @author KFOKAM48
 * @version 1.0
 */
@Entity
@Table(
    name = "users",
    // Contrainte d'unicité sur l'email pour éviter les doublons en BDD
    uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Évite les boucles infinies dans toString/equals avec les collections Spring Security
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

    /**
     * Identifiant unique auto-généré.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Nom complet de l'utilisateur.
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    /**
     * Adresse email — sert d'identifiant de connexion (username).
     * Doit être unique en base de données.
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Mot de passe hashé avec BCrypt.
     * Ne jamais stocker ou retourner le mot de passe en clair !
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    /**
     * Rôle de l'utilisateur (ROLE_USER par défaut).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    /**
     * Indique si le compte est actif.
     * Un compte désactivé ne peut pas se connecter.
     */
    @Column(name = "actif", nullable = false)
    @Builder.Default
    private boolean actif = true;

    /**
     * Date de création — remplie automatiquement à l'insertion.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification — mise à jour automatiquement.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =====================================================
    //  Implémentation de UserDetails (Spring Security)
    // =====================================================

    /**
     * Retourne les autorités/rôles de l'utilisateur.
     * Spring Security utilise cela pour les vérifications d'accès.
     *
     * @return collection contenant le rôle de l'utilisateur
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertit le rôle enum en GrantedAuthority Spring Security
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Retourne le mot de passe hashé (requis par UserDetails).
     */
    @Override
    public String getPassword() {
        return motDePasse;
    }

    /**
     * Retourne l'email comme nom d'utilisateur (identifiant de connexion).
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indique si le compte n'est pas expiré.
     * Retourne toujours true (pas de gestion d'expiration dans ce projet).
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indique si le compte n'est pas verrouillé.
     */
    @Override
    public boolean isAccountNonLocked() {
        return actif;
    }

    /**
     * Indique si les credentials ne sont pas expirés.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indique si le compte est activé.
     */
    @Override
    public boolean isEnabled() {
        return actif;
    }
}
