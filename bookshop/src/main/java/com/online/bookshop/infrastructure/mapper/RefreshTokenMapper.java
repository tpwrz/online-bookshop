package com.online.bookshop.infrastructure.mapper;

import com.online.bookshop.domain.model.RefreshToken;
import com.online.bookshop.infrastructure.persistence.RefreshTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    public RefreshTokenEntity toEntity(RefreshToken domain) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setToken(domain.getToken());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setRevoked(domain.isRevoked());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        return entity;
    }

    public RefreshToken toDomain(RefreshTokenEntity entity) {
        RefreshToken domain = new RefreshToken();
        domain.setId(entity.getId());
        domain.setUserId(entity.getUserId());
        domain.setToken(entity.getToken());
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setRevoked(entity.isRevoked());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
