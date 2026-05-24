package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.UserService;
import com.online.bookshop.domain.model.Review;
import com.online.bookshop.domain.model.User;
import com.online.bookshop.domain.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRegistrationDate(LocalDate.of(2024, 1, 1));
    }

    // ===================== GET /users/me =====================

    @Test
    @DisplayName("getCurrentUserInfo: пользователь найден — возвращает 200 без пароля")
    void getCurrentUserInfo_found() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        ResponseEntity<UserController.UserResponse> response =
                userController.getCurrentUserInfo(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("testuser");
        assertThat(response.getBody().email()).isEqualTo("test@example.com");
        assertThat(response.getBody().status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("getCurrentUserInfo: пользователь не найден — возвращает 404")
    void getCurrentUserInfo_notFound() {
        when(authentication.getName()).thenReturn("unknown");
        when(userService.findByUsername("unknown")).thenReturn(Optional.empty());

        ResponseEntity<UserController.UserResponse> response =
                userController.getCurrentUserInfo(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ===================== GET /users =====================

    @Test
    @DisplayName("getAllUsers: возвращает список пользователей без паролей")
    void getAllUsers_returnsList() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setStatus(UserStatus.ACTIVE);

        when(userService.findAll()).thenReturn(List.of(testUser, user2));

        ResponseEntity<List<UserController.UserResponse>> response = userController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).username()).isEqualTo("testuser");
        assertThat(response.getBody().get(1).username()).isEqualTo("user2");
    }

    @Test
    @DisplayName("getAllUsers: пустой список — возвращает 200 с пустым массивом")
    void getAllUsers_empty() {
        when(userService.findAll()).thenReturn(List.of());

        ResponseEntity<List<UserController.UserResponse>> response = userController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // ===================== GET /users/{id} =====================

    @Test
    @DisplayName("getUserById: пользователь найден — возвращает 200")
    void getUserById_found() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<UserController.UserResponse> response = userController.getUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getUserById: пользователь не найден — возвращает 404")
    void getUserById_notFound() {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<UserController.UserResponse> response = userController.getUserById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ===================== GET /users/email =====================

    @Test
    @DisplayName("getUserByEmail: найден — возвращает 200")
    void getUserByEmail_found() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        ResponseEntity<UserController.UserResponse> response =
                userController.getUserByEmail("test@example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("getUserByEmail: не найден — возвращает 404")
    void getUserByEmail_notFound() {
        when(userService.findByEmail("none@example.com")).thenReturn(Optional.empty());

        ResponseEntity<UserController.UserResponse> response =
                userController.getUserByEmail("none@example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ===================== GET /users/username =====================

    @Test
    @DisplayName("getUserByUsername: найден — возвращает 200")
    void getUserByUsername_found() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        ResponseEntity<UserController.UserResponse> response =
                userController.getUserByUsername("testuser");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().username()).isEqualTo("testuser");
    }

    // ===================== GET /users/status =====================

    @Test
    @DisplayName("getUsersByStatus: возвращает только пользователей с нужным статусом")
    void getUsersByStatus_returnsFiltered() {
        when(userService.findByStatus(UserStatus.ACTIVE)).thenReturn(List.of(testUser));

        ResponseEntity<List<UserController.UserResponse>> response =
                userController.getUsersByStatus(UserStatus.ACTIVE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).status()).isEqualTo("ACTIVE");
    }

    // ===================== GET /users/searchByUsernameOrEmail =====================

    @Test
    @DisplayName("searchUsers: возвращает совпадения")
    void searchUsers_returnsResults() {
        when(userService.findByUsernameOrEmailContaining("test")).thenReturn(List.of(testUser));

        ResponseEntity<List<UserController.UserResponse>> response =
                userController.searchUsers("test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("searchUsers: нет совпадений — возвращает пустой список")
    void searchUsers_empty() {
        when(userService.findByUsernameOrEmailContaining("xyz")).thenReturn(List.of());

        ResponseEntity<List<UserController.UserResponse>> response =
                userController.searchUsers("xyz");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // ===================== PUT /users/{id} =====================

    @Test
    @DisplayName("updateUser: владелец обновляет себя — возвращает 200")
    void updateUser_ownerUpdates() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        testUser.setEmail("new@example.com");
        when(userService.save(any(User.class))).thenReturn(testUser);

        UserController.UpdateUserRequest request = new UserController.UpdateUserRequest("new@example.com");
        ResponseEntity<UserController.UserResponse> response =
                userController.updateUser(1L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().email()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("updateUser: чужой id — возвращает 403")
    void updateUser_forbiddenForOtherUser() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserController.UpdateUserRequest request = new UserController.UpdateUserRequest("other@example.com");
        ResponseEntity<UserController.UserResponse> response =
                userController.updateUser(999L, request, authentication); // чужой id

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(userService, never()).save(any());
    }

    // ===================== DELETE /users/{id} =====================

    @Test
    @DisplayName("deleteUser: владелец удаляет себя — возвращает 204")
    void deleteUser_ownerDeletes() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        ResponseEntity<Void> response = userController.deleteUser(1L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser: чужой id — возвращает 403")
    void deleteUser_forbiddenForOtherUser() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        ResponseEntity<Void> response = userController.deleteUser(999L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(userService, never()).deleteById(any());
    }

    // ===================== GET /users/{id}/reviews =====================

    @Test
    @DisplayName("getReviewsByUser: есть reviews — возвращает 200 со списком")
    void getReviewsByUser_found() {
        Review review = new Review(1L, 1L, 1L, "Great book!", 5);
        when(userService.getReviewsByUserId(1L)).thenReturn(List.of(review));
        ResponseEntity<List<Review>> response = userController.getReviewsByUser(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getReviewMessage()).isEqualTo("Great book!");
    }

    @Test
    @DisplayName("getReviewsByUser: нет reviews — возвращает 404")
    void getReviewsByUser_empty() {
        when(userService.getReviewsByUserId(1L)).thenReturn(List.of());

        ResponseEntity<List<Review>> response = userController.getReviewsByUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ===================== UserResponse =====================

    @Test
    @DisplayName("UserResponse.from: не содержит пароль")
    void userResponse_noPassword() {
        testUser.setPassword("$2a$12$hashedpassword");

        UserController.UserResponse response = UserController.UserResponse.from(testUser);

        // UserResponse не имеет поля password — проверяем что маппинг корректный
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.registrationDate()).isEqualTo("2024-01-01");
    }

    @Test
    @DisplayName("UserResponse.from: null status и null date обрабатываются безопасно")
    void userResponse_nullFields() {
        User userWithNulls = new User();
        userWithNulls.setId(2L);
        userWithNulls.setUsername("user2");
        userWithNulls.setEmail("u2@example.com");
        // status и registrationDate = null

        UserController.UserResponse response = UserController.UserResponse.from(userWithNulls);

        assertThat(response.status()).isNull();
        assertThat(response.registrationDate()).isNull();
    }
}
