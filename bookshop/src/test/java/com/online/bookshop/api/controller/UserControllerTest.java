package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.UserService;
import com.online.bookshop.domain.model.Review;
import com.online.bookshop.domain.model.User;
import com.online.bookshop.domain.model.enums.UserRole;
import com.online.bookshop.domain.model.enums.UserStatus;
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
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController — unit tests")
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(UserRole.USER);
        testUser.setRegistrationDate(LocalDate.of(2024, 1, 1));

        adminUser = new User();
        adminUser.setId(99L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@bookshop.com");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setRegistrationDate(LocalDate.of(2024, 1, 1));
    }

    @Nested
    @DisplayName("GET /users/me")
    class GetMe {

        @Test
        @DisplayName("пользователь найден — возвращает 200 без пароля")
        void found() {
            when(authentication.getName()).thenReturn("testuser");
            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            ResponseEntity<UserController.UserResponse> response =
                    userController.getCurrentUserInfo(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().username()).isEqualTo("testuser");
            assertThat(response.getBody().email()).isEqualTo("test@example.com");
            assertThat(response.getBody().status()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("пользователь не найден — возвращает 404")
        void notFound() {
            when(authentication.getName()).thenReturn("unknown");
            when(userService.findByUsername("unknown")).thenReturn(Optional.empty());

            ResponseEntity<UserController.UserResponse> response =
                    userController.getCurrentUserInfo(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /users (ADMIN only)")
    class GetAll {

        @Test
        @DisplayName("возвращает список всех пользователей")
        void returnsList() {
            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");
            user2.setStatus(UserStatus.ACTIVE);
            user2.setRole(UserRole.USER);

            when(userService.findAll()).thenReturn(List.of(testUser, user2));

            ResponseEntity<List<UserController.UserResponse>> response =
                    userController.getAllUsers();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }

        @Test
        @DisplayName("пустой список — возвращает 200 с пустым массивом")
        void empty() {
            when(userService.findAll()).thenReturn(List.of());

            ResponseEntity<List<UserController.UserResponse>> response =
                    userController.getAllUsers();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /users/{id} (ADMIN only)")
    class GetById {

        @Test
        @DisplayName("пользователь найден — возвращает 200")
        void found() {
            when(userService.findById(1L)).thenReturn(Optional.of(testUser));

            ResponseEntity<UserController.UserResponse> response =
                    userController.getUserById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("пользователь не найден — возвращает 404")
        void notFound() {
            when(userService.findById(99L)).thenReturn(Optional.empty());

            assertThat(userController.getUserById(99L).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /users/email")
    class GetByEmail {

        @Test
        @DisplayName("найден — возвращает 200")
        void found() {
            when(userService.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(testUser));

            ResponseEntity<UserController.UserResponse> response =
                    userController.getUserByEmail("test@example.com");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().email()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("не найден — возвращает 404")
        void notFound() {
            when(userService.findByEmail("none@example.com"))
                    .thenReturn(Optional.empty());

            assertThat(userController.getUserByEmail("none@example.com").getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /users/username")
    class GetByUsername {

        @Test
        @DisplayName("найден — возвращает 200")
        void found() {
            when(userService.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));

            ResponseEntity<UserController.UserResponse> response =
                    userController.getUserByUsername("testuser");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("не найден — возвращает 404")
        void notFound() {
            when(userService.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThat(userController.getUserByUsername("ghost").getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /users/status")
    class GetByStatus {

        @Test
        @DisplayName("возвращает только пользователей с нужным статусом")
        void returnsFiltered() {
            when(userService.findByStatus(UserStatus.ACTIVE))
                    .thenReturn(List.of(testUser));

            ResponseEntity<List<UserController.UserResponse>> response =
                    userController.getUsersByStatus(UserStatus.ACTIVE);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).status()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("GET /users/searchByUsernameOrEmail")
    class Search {

        @Test
        @DisplayName("возвращает совпадения")
        void returnsResults() {
            when(userService.findByUsernameOrEmailContaining("test"))
                    .thenReturn(List.of(testUser));

            ResponseEntity<List<UserController.UserResponse>> response =
                    userController.searchUsers("test");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("нет совпадений — возвращает пустой список")
        void empty() {
            when(userService.findByUsernameOrEmailContaining("xyz"))
                    .thenReturn(List.of());

            ResponseEntity<List<UserController.UserResponse>> response =
                    userController.searchUsers("xyz");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PUT /users/{id}")
    class Update {

        @Test
        @DisplayName("владелец обновляет себя — возвращает 200")
        void ownerUpdates() {
            when(authentication.getName()).thenReturn("testuser");
            when(userService.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            testUser.setEmail("new@example.com");
            when(userService.save(any(User.class))).thenReturn(testUser);

            UserController.UpdateUserRequest request =
                    new UserController.UpdateUserRequest("new@example.com");
            ResponseEntity<UserController.UserResponse> response =
                    userController.updateUser(1L, request, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().email()).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("USER пытается обновить чужой аккаунт — возвращает 403")
        void forbiddenForOtherUser() {
            when(authentication.getName()).thenReturn("testuser");
            when(userService.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));

            UserController.UpdateUserRequest request =
                    new UserController.UpdateUserRequest("other@example.com");
            ResponseEntity<UserController.UserResponse> response =
                    userController.updateUser(999L, request, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            verify(userService, never()).save(any());
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id}")
    class Delete {

        @Test
        @DisplayName("владелец удаляет себя — возвращает 204")
        void ownerDeletes() {
            when(authentication.getName()).thenReturn("testuser");
            when(userService.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));

            ResponseEntity<Void> response =
                    userController.deleteUser(1L, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(userService).deleteById(1L);
        }

        @Test
        @DisplayName("USER пытается удалить чужой аккаунт — возвращает 403")
        void forbiddenForOtherUser() {
            when(authentication.getName()).thenReturn("testuser");
            when(userService.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));

            ResponseEntity<Void> response =
                    userController.deleteUser(999L, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            verify(userService, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("GET /users/{id}/reviews")
    class GetReviews {

        @Test
        @DisplayName("есть reviews — возвращает 200 со списком")
        void found() {
            Review review = new Review(1L, 1L, 1L, "Great book!", 5);
            when(userService.getReviewsByUserId(1L)).thenReturn(List.of(review));

            ResponseEntity<List<Review>> response =
                    userController.getReviewsByUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getReviewMessage()).isEqualTo("Great book!");
        }

        @Test
        @DisplayName("нет reviews — возвращает 404")
        void empty() {
            when(userService.getReviewsByUserId(1L)).thenReturn(List.of());

            ResponseEntity<List<Review>> response =
                    userController.getReviewsByUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("UserResponse.from()")
    class UserResponseMapping {

        @Test
        @DisplayName("не содержит пароль, все поля маппятся корректно")
        void noPassword() {
            testUser.setPassword("$2a$12$hashedpassword");

            UserController.UserResponse response =
                    UserController.UserResponse.from(testUser);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.username()).isEqualTo("testuser");
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.registrationDate()).isEqualTo("2024-01-01");
        }

        @Test
        @DisplayName("null status и null date обрабатываются безопасно")
        void nullFields() {
            User userWithNulls = new User();
            userWithNulls.setId(2L);
            userWithNulls.setUsername("user2");
            userWithNulls.setEmail("u2@example.com");

            UserController.UserResponse response =
                    UserController.UserResponse.from(userWithNulls);

            assertThat(response.status()).isNull();
            assertThat(response.registrationDate()).isNull();
        }
    }
}