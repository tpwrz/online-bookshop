package com.online.bookshop.application.service;

import com.online.bookshop.api.dto.AuthDtos.AuthResponse;
import com.online.bookshop.api.dto.AuthDtos.LoginRequest;
import com.online.bookshop.api.dto.AuthDtos.RefreshRequest;
import com.online.bookshop.api.dto.AuthDtos.RegisterRequest;
import com.online.bookshop.api.security.JwtService;
import com.online.bookshop.domain.model.RefreshToken;
import com.online.bookshop.domain.model.User;
import com.online.bookshop.domain.model.enums.UserStatus;
import com.online.bookshop.domain.repository.RefreshTokenRepository;
import com.online.bookshop.domain.repository.UserRepository;
import com.online.bookshop.infrastructure.persistence.PersonEntity;
import com.online.bookshop.infrastructure.repository.JpaPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JpaPersonRepository personJpaRepository;
    @Value("${jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        PersonEntity person = new PersonEntity();
        person.setFirstName(request.firstName());
        person.setLastName(request.lastName());
        person.setBirthDate(LocalDate.parse(request.birthDate()));
        personJpaRepository.save(person);

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRegistrationDate(LocalDate.now());
        user.setPersonId(person.getId());

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());

        return issueTokenPair(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        refreshTokenRepository.revokeAllByUserId(user.getId());

        return issueTokenPair(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (!stored.isValid()) {
            refreshTokenRepository.revokeAllByUserId(stored.getUserId());
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokenPair(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    refreshTokenRepository.revokeAllByUserId(token.getUserId());
                    log.info("User {} logged out", token.getUserId());
                });
    }

    private AuthResponse issueTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId());
        String rawRefreshToken = jwtService.generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setToken(rawRefreshToken);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(accessToken, rawRefreshToken, accessTokenExpirationMs);
    }
}
