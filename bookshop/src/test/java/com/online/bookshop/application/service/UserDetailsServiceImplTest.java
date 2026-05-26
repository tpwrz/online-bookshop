package com.online.bookshop.application.service;

import com.online.bookshop.api.security.UserDetailsServiceImpl;
import com.online.bookshop.domain.model.enums.UserRole;
import com.online.bookshop.infrastructure.persistence.UserEntity;
import com.online.bookshop.infrastructure.repository.JpaUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl — unit tests")
class UserDetailsServiceImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsername {

        @Test
        @DisplayName("обычный пользователь получает ROLE_USER")
        void returnsRoleUser() {
            UserEntity entity = new UserEntity();
            entity.setUsername("john");
            entity.setPassword("$2a$12$hashed");
            entity.setRole(UserRole.USER);

            when(jpaUserRepository.findByUsername("john"))
                    .thenReturn(Optional.of(entity));

            UserDetails result = service.loadUserByUsername("john");

            assertThat(result.getUsername()).isEqualTo("john");
            assertThat(result.getPassword()).isEqualTo("$2a$12$hashed");
            assertThat(result.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("админ получает ROLE_ADMIN")
        void returnsRoleAdmin() {
            UserEntity entity = new UserEntity();
            entity.setUsername("admin");
            entity.setPassword("$2a$12$hashed");
            entity.setRole(UserRole.ADMIN);

            when(jpaUserRepository.findByUsername("admin"))
                    .thenReturn(Optional.of(entity));

            UserDetails result = service.loadUserByUsername("admin");

            assertThat(result.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("role == null — дефолтно ROLE_USER")
        void nullRoleDefaultsToUser() {
            UserEntity entity = new UserEntity();
            entity.setUsername("norole");
            entity.setPassword("pass");
            entity.setRole(null);

            when(jpaUserRepository.findByUsername("norole"))
                    .thenReturn(Optional.of(entity));

            UserDetails result = service.loadUserByUsername("norole");

            assertThat(result.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("пользователь не найден — выбрасывает UsernameNotFoundException")
        void throwsWhenNotFound() {
            when(jpaUserRepository.findByUsername("ghost"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("ROLE_USER не имеет доступа к ADMIN-эндпоинтам — авторитет не содержит ROLE_ADMIN")
        void userRoleDoesNotContainAdminAuthority() {
            UserEntity entity = new UserEntity();
            entity.setUsername("regularuser");
            entity.setPassword("pass");
            entity.setRole(UserRole.USER);

            when(jpaUserRepository.findByUsername("regularuser"))
                    .thenReturn(Optional.of(entity));

            UserDetails result = service.loadUserByUsername("regularuser");

            assertThat(result.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .doesNotContain("ROLE_ADMIN");
        }
    }
}