package com.online.bookshop.application.service;

import com.online.bookshop.api.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-256-bits-long-enough-for-hs256!!";
    private static final long EXPIRATION_MS = 900000L; // 15 min

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateAccessToken: возвращает непустой токен")
    void generateAccessToken_notEmpty() {
        String token = jwtService.generateAccessToken("testuser", 1L);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateAccessToken: извлечённый username совпадает")
    void generateAccessToken_usernameExtracted() {
        String token = jwtService.generateAccessToken("testuser", 1L);
        assertThat(jwtService.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    @DisplayName("generateAccessToken: извлечённый userId совпадает")
    void generateAccessToken_userIdExtracted() {
        String token = jwtService.generateAccessToken("testuser", 42L);
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("validateAccessToken: валидный токен возвращает true")
    void validateAccessToken_valid() {
        String token = jwtService.generateAccessToken("testuser", 1L);
        assertThat(jwtService.validateAccessToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateAccessToken: испорченный токен возвращает false")
    void validateAccessToken_malformed() {
        assertThat(jwtService.validateAccessToken("this.is.not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("validateAccessToken: пустая строка возвращает false")
    void validateAccessToken_empty() {
        assertThat(jwtService.validateAccessToken("")).isFalse();
    }

    @Test
    @DisplayName("validateAccessToken: истёкший токен возвращает false")
    void validateAccessToken_expired() {
        // Генерируем токен с истёкшим временем вручную
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 1L)
                .claim("type", "access")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // уже истёк
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThat(jwtService.validateAccessToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("validateAccessToken: токен подписан другим ключом возвращает false")
    void validateAccessToken_wrongKey() {
        Key otherKey = Keys.hmacShaKeyFor("other-secret-key-256-bits-long-enough!!!!!".getBytes());
        String tokenWithWrongKey = Jwts.builder()
                .setSubject("testuser")
                .claim("type", "access")
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(otherKey, SignatureAlgorithm.HS256)
                .compact();

        assertThat(jwtService.validateAccessToken(tokenWithWrongKey)).isFalse();
    }

    @Test
    @DisplayName("validateAccessToken: токен без claim type=access возвращает false")
    void validateAccessToken_wrongType() {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String tokenWithoutType = Jwts.builder()
                .setSubject("testuser")
                .claim("type", "refresh") // не access
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThat(jwtService.validateAccessToken(tokenWithoutType)).isFalse();
    }

    @Test
    @DisplayName("generateRefreshToken: возвращает непустой UUID-like токен")
    void generateRefreshToken_notEmpty() {
        String token = jwtService.generateRefreshToken();
        assertThat(token).isNotBlank();
        assertThat(token).matches("[0-9a-f-]{36}"); // UUID format
    }

    @Test
    @DisplayName("generateRefreshToken: каждый вызов возвращает уникальный токен")
    void generateRefreshToken_unique() {
        String token1 = jwtService.generateRefreshToken();
        String token2 = jwtService.generateRefreshToken();
        assertThat(token1).isNotEqualTo(token2);
    }
}
