package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.OrderService;
import com.online.bookshop.domain.model.Order;
import com.online.bookshop.domain.model.enums.OrderStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController — unit tests")
class OrderControllerTest {

    @Mock
    private OrderService service;

    @InjectMocks
    private OrderController controller;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(
                1L,
                "ship test",
                LocalDate.now(),
                OrderStatus.NEW,
                1L,
                List.of()
        );
    }

    @Nested
    @DisplayName("GET /orders")
    class GetAll {

        @Test
        @DisplayName("returns 200 with list of orders")
        void returnsAllOrders() {
            when(service.findAll()).thenReturn(List.of(order));

            ResponseEntity<List<Order>> response = controller.getAllOrders();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("returns 200 with empty list when no orders")
        void returnsEmptyList() {
            when(service.findAll()).thenReturn(List.of());

            assertThat(controller.getAllOrders().getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /orders/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 when found")
        void returnsOrderWhenFound() {
            when(service.findById(1L)).thenReturn(Optional.of(order));

            ResponseEntity<Order> response = controller.getOrderById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(service.findById(99L)).thenReturn(Optional.empty());

            assertThat(controller.getOrderById(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /orders")
    class Create {

        @Test
        @DisplayName("returns 201 with created order")
        void returns201WithOrder() {
            Order input = new Order(1L, "ship test", LocalDate.now(), OrderStatus.NEW, 1L, List.of());
            when(service.save(input)).thenReturn(order);

            ResponseEntity<Order> response = controller.createOrder(input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Assertions.assertNotNull(response.getBody());
            assertThat(response.getBody().getId()).isEqualTo(1L);
            verify(service).save(input);
        }
    }

    @Nested
    @DisplayName("PUT /orders/{id}")
    class Update {

        @Test
        @DisplayName("sets id from path and returns 200")
        void setsIdAndReturnsUpdated() {
            Order input = new Order(1L, "ship test", LocalDate.now(), OrderStatus.NEW, 1L, List.of());
            when(service.save(any(Order.class))).thenReturn(order);

            ResponseEntity<Order> response = controller.updateOrder(1L, input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).save(argThat(o -> o.getId().equals(1L)));
        }
    }

    @Nested
    @DisplayName("DELETE /orders/{id}")
    class Delete {

        @Test
        @DisplayName("returns 200 and delegates to service")
        void delegatesToService() {
            doNothing().when(service).deleteById(1L);

            ResponseEntity<Void> response = controller.deleteOrder(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("GET /orders/byUser/{userId}")
    class ByUser {

        @Test
        @DisplayName("returns 200 with orders for user")
        void returnsOrdersForUser() {
            when(service.findByUserId(1L)).thenReturn(List.of(order));

            ResponseEntity<List<Order>> response = controller.getOrdersByUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("returns 200 with empty list when user has no orders")
        void returnsEmptyForUser() {
            when(service.findByUserId(99L)).thenReturn(List.of());

            assertThat(controller.getOrdersByUser(99L).getBody()).isEmpty();
        }
    }
}