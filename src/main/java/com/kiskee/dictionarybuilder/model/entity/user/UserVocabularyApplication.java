package com.kiskee.dictionarybuilder.model.entity.user;

import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.model.entity.user.preference.UserPreference;
import com.kiskee.dictionarybuilder.model.entity.user.profile.UserProfile;
import com.kiskee.dictionarybuilder.repository.user.projections.UserSecureProjection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Entity
@AllArgsConstructor
@Builder(setterPrefix = "set", toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserVocabularyApplication implements UserDetails, UserSecureProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
    private UserProfile userProfile;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
    private UserPreference userPreference;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(getRole().name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }

    public UserVocabularyApplication setActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }
}
