package com.online.bookshop.api.dto;

public class AuthDtos {

    public record RegisterRequest(
            String username,
            String email,
            String password,
            String firstName,
            String lastName,
            String birthDate
    ) {
    }

    public record LoginRequest(
            String username,
            String password
    ) {
    }

    public record RefreshRequest(
            String refreshToken
    ) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn   // seconds
    ) {
        public static AuthResponse of(String accessToken, String refreshToken, long expiresInMs) {
            return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInMs / 1000);
        }
    }

    public record MessageResponse(String message) {
    }
}
