package com.online.bookshop.infrastructure.repository;

import com.online.bookshop.domain.model.RefreshToken;
import com.online.bookshop.domain.repository.RefreshTokenRepository;
import com.online.bookshop.infrastructure.mapper.RefreshTokenMapper;
import com.online.bookshop.infrastructure.persistence.RefreshTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository jpa;
    private final RefreshTokenMapper mapper;

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity entity = mapper.toEntity(token);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpa.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public void revokeAllByUserId(Long userId) {
        jpa.revokeAllByUserId(userId);
    }

    @Override
    public void deleteExpired() {
        jpa.deleteExpiredBefore(LocalDateTime.now());
    }
}
