package com.online.bookshop.application.service;

import com.online.bookshop.api.dto.AuthDtos.AuthResponse;
import com.online.bookshop.api.dto.AuthDtos.LoginRequest;
import com.online.bookshop.api.dto.AuthDtos.RefreshRequest;
import com.online.bookshop.api.dto.AuthDtos.RegisterRequest;
import com.online.bookshop.api.security.JwtService;
import com.online.bookshop.domain.model.RefreshToken;
import com.online.bookshop.domain.model.User;
import com.online.bookshop.domain.model.enums.UserRole;
import com.online.bookshop.domain.model.enums.UserStatus;
import com.online.bookshop.domain.repository.RefreshTokenRepository;
import com.online.bookshop.domain.repository.UserRepository;
import com.online.bookshop.infrastructure.persistence.PersonEntity;
import com.online.bookshop.infrastructure.repository.JpaPersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JpaPersonRepository personJpaRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs", 900000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationDays", 7L);
    }

    @Test
    @DisplayName("register: успешная регистрация возвращает токены")
    void register_success() {
        RegisterRequest request = new RegisterRequest(
                "testuser", "test@example.com", "Password123!",
                "Test", "User", "2000-01-01");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$12$hashed");

        PersonEntity savedPerson = new PersonEntity();
        savedPerson.setId(1L);
        when(personJpaRepository.save(any(PersonEntity.class))).thenReturn(savedPerson);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtService.generateAccessToken("testuser", 1L)).thenReturn("access-token");
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<String> response = authService.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("OK");

        verify(userRepository).save(argThat(u ->
                u.getUsername().equals("testuser") &&
                        u.getPassword().equals("$2a$12$hashed") &&
                        u.getStatus() == UserStatus.ACTIVE &&
                        u.getRole() == UserRole.USER          // NEW
        ));
    }

    @Test
    @DisplayName("register: username занят — выбрасывает IllegalArgumentException")
    void register_usernameTaken() {
        RegisterRequest request = new RegisterRequest(
                "testuser", "test@example.com", "Password123!",
                "Test", "User", "2000-01-01");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: email занят — выбрасывает IllegalArgumentException")
    void register_emailTaken() {
        RegisterRequest request = new RegisterRequest(
                "testuser", "test@example.com", "Password123!",
                "Test", "User", "2000-01-01");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: PersonEntity сохраняется с правильными данными")
    void register_personSavedCorrectly() {
        RegisterRequest request = new RegisterRequest(
                "testuser", "test@example.com", "Password123!",
                "John", "Doe", "1990-06-15");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        PersonEntity savedPerson = new PersonEntity();
        savedPerson.setId(5L);
        when(personJpaRepository.save(any())).thenReturn(savedPerson);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("at");
        when(jwtService.generateRefreshToken()).thenReturn("rt");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.register(request);

        ArgumentCaptor<PersonEntity> personCaptor = ArgumentCaptor.forClass(PersonEntity.class);
        verify(personJpaRepository).save(personCaptor.capture());
        PersonEntity capturedPerson = personCaptor.getValue();

        assertThat(capturedPerson.getFirstName()).isEqualTo("John");
        assertThat(capturedPerson.getLastName()).isEqualTo("Doe");
        assertThat(capturedPerson.getBirthDate().toString()).isEqualTo("1990-06-15");
    }

    @Test
    @DisplayName("login: успешный логин возвращает токены и отзывает старые refresh tokens")
    void login_success() {
        LoginRequest request = new LoginRequest("testuser", "Password123!");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("testuser", 1L)).thenReturn("access-token");
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(authenticationManager).authenticate(
                argThat(a -> a instanceof UsernamePasswordAuthenticationToken &&
                        a.getPrincipal().equals("testuser")));
        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    @DisplayName("login: неверные credentials — выбрасывает BadCredentialsException")
    void login_badCredentials() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("refresh: валидный токен — возвращает новую пару токенов")
    void refresh_success() {
        RefreshRequest request = new RefreshRequest("valid-refresh-token");

        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken("valid-refresh-token");
        storedToken.setUserId(1L);
        storedToken.setRevoked(false);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(refreshTokenRepository.findByToken("valid-refresh-token"))
                .thenReturn(Optional.of(storedToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("testuser", 1L)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken()).thenReturn("new-refresh-token");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");

        // Старый токен должен быть revoked
        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("refresh: токен не найден — выбрасывает IllegalArgumentException")
    void refresh_tokenNotFound() {
        RefreshRequest request = new RefreshRequest("unknown-token");

        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token not found");
    }

    @Test
    @DisplayName("refresh: токен истёк — отзывает все токены пользователя")
    void refresh_expiredToken() {
        RefreshRequest request = new RefreshRequest("expired-token");

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setUserId(1L);
        expiredToken.setRevoked(false);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // expired

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired or revoked");

        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    @DisplayName("refresh: токен revoked — отзывает все токены пользователя")
    void refresh_revokedToken() {
        RefreshRequest request = new RefreshRequest("revoked-token");

        RefreshToken revokedToken = new RefreshToken();
        revokedToken.setToken("revoked-token");
        revokedToken.setUserId(1L);
        revokedToken.setRevoked(true);
        revokedToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByToken("revoked-token"))
                .thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired or revoked");

        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    @DisplayName("logout: валидный токен — отзывает все токены пользователя")
    void logout_success() {
        RefreshToken token = new RefreshToken();
        token.setToken("some-refresh-token");
        token.setUserId(1L);

        when(refreshTokenRepository.findByToken("some-refresh-token"))
                .thenReturn(Optional.of(token));

        authService.logout("some-refresh-token");

        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    @DisplayName("logout: несуществующий токен — ничего не происходит")
    void logout_tokenNotFound() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatCode(() -> authService.logout("unknown")).doesNotThrowAnyException();

        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    @DisplayName("register: новый пользователь получает роль USER")
    void register_assignsRoleUser() {
        RegisterRequest request = new RegisterRequest(
                "testuser", "test@example.com", "Password123!",
                "Test", "User", "2000-01-01");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        PersonEntity savedPerson = new PersonEntity();
        savedPerson.setId(1L);
        when(personJpaRepository.save(any())).thenReturn(savedPerson);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("at");
        when(jwtService.generateRefreshToken()).thenReturn("rt");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.USER);
    }
}
