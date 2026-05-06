package com.online.bookshop.domain.repository;

import com.online.bookshop.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void revokeAllByUserId(Long userId);

    void deleteExpired();
}
