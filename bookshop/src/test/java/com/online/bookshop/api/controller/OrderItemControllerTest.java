package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.OrderItemService;
import com.online.bookshop.domain.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderItemController — unit tests")
class OrderItemControllerTest {

    @Mock
    private OrderItemService service;

    @InjectMocks
    private OrderItemController controller;

    private OrderItem item;

    @BeforeEach
    void setUp() {
        item = new OrderItem(1L, 1L, 1L, 2, 9.99);
    }

    // ---------------------------------------------------------------- getAll

    @Nested
    @DisplayName("GET /order-items")
    class GetAll {

        @Test
        @DisplayName("returns list of order items")
        void returnsAll() {
            when(service.findAll()).thenReturn(List.of(item));

            List<OrderItem> result = controller.getAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns empty list when no items")
        void returnsEmpty() {
            when(service.findAll()).thenReturn(List.of());

            assertThat(controller.getAll()).isEmpty();
        }
    }

    // --------------------------------------------------------------- getById

    @Nested
    @DisplayName("GET /order-items/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 when found")
        void returnsItemWhenFound() {
            when(service.findById(1L)).thenReturn(Optional.of(item));

            ResponseEntity<OrderItem> response = controller.getById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(service.findById(99L)).thenReturn(Optional.empty());

            assertThat(controller.getById(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ---------------------------------------------------------------- create

    @Nested
    @DisplayName("POST /order-items")
    class Create {

        @Test
        @DisplayName("returns 200 with saved item")
        void returnsSavedItem() {
            OrderItem input = new OrderItem(1L, 1L, 1L, 5, 20d);
            input.setBookId(2L);
            when(service.save(input)).thenReturn(item);

            ResponseEntity<OrderItem> response = controller.create(input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(1L);
            verify(service).save(input);
        }
    }

    // ---------------------------------------------------------------- update

    @Nested
    @DisplayName("PUT /order-items/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with updated item when found")
        void returnsUpdatedItem() {
            OrderItem input = new OrderItem(1L, 1L, 1L, 5, 20d);
            when(service.findById(1L)).thenReturn(Optional.of(item));
            when(service.save(any(OrderItem.class))).thenReturn(item);

            ResponseEntity<OrderItem> response = controller.update(1L, input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).save(argThat(i -> i.getId().equals(1L)));
        }

        @Test
        @DisplayName("returns 404 when item not found")
        void returns404WhenNotFound() {
            when(service.findById(99L)).thenReturn(Optional.empty());

            ResponseEntity<OrderItem> response = controller.update(99L, new OrderItem(1L, 1L, 1L, 5, 20d));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(service, never()).save(any());
        }
    }

    // ---------------------------------------------------------------- delete

    @Nested
    @DisplayName("DELETE /order-items/{id}")
    class Delete {

        @Test
        @DisplayName("returns 200 and delegates to service")
        void delegatesToService() {
            doNothing().when(service).deleteById(1L);

            ResponseEntity<Void> response = controller.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).deleteById(1L);
        }
    }

    // --------------------------------------------------------- byOrderId

    @Nested
    @DisplayName("GET /order-items/order/{orderId}")
    class ByOrderId {

        @Test
        @DisplayName("returns items for order")
        void returnsItemsForOrder() {
            when(service.findByOrderId(1L)).thenReturn(List.of(item));

            List<OrderItem> result = controller.getByOrderId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOrderId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns empty list when order has no items")
        void returnsEmptyForOrder() {
            when(service.findByOrderId(99L)).thenReturn(List.of());

            assertThat(controller.getByOrderId(99L)).isEmpty();
        }
    }
}