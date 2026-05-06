package com.online.bookshop.infrastructure.mapper;

import com.online.bookshop.domain.model.Order;
import com.online.bookshop.infrastructure.persistence.OrderEntity;
import com.online.bookshop.infrastructure.persistence.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public static Order toDomain(OrderEntity entity) {
        if (entity == null) return null;

        return new Order(
                entity.getId(),
                entity.getShipAddress(),
                entity.getOrderDate(),
                entity.getOrderStatus(),
                entity.getUser().getId(),
                entity.getItems()
                        .stream()
                        .map(OrderItemMapper::toDomain)
                        .collect(Collectors.toList())
        );
    }

    public static OrderEntity toEntity(Order domain) {
        if (domain == null) return null;

        OrderEntity entity = new OrderEntity();
        if (domain.getId() != 0) {
            entity.setId(domain.getId());
        }
        entity.setShipAddress(domain.getShipAddress());
        entity.setOrderDate(domain.getOrderDate());
        entity.setOrderStatus(domain.getOrderStatus());
        entity.setUser(UserMapper.refUser(domain.getUserId()));
        entity.setItems(
                domain.getItems()
                        .stream()
                        .map(item -> {
                            OrderItemEntity itemEntity = OrderItemMapper.toEntity(item);
                            itemEntity.setOrder(entity);
                            return itemEntity;
                        })
                        .collect(Collectors.toList())
        );

        return entity;
    }

    public static OrderEntity refOrder(Long orderId) {
        if (orderId == null) return null;
        OrderEntity entity = new OrderEntity();
        entity.setId(orderId);
        return entity;
    }
}
