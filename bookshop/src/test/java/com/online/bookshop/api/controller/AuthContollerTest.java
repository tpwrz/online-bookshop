package com.online.bookshop.api.controller;

import com.online.bookshop.api.dto.AuthDtos.*;
import com.online.bookshop.application.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController — unit tests")
class AuthControllerTest {
    private final AuthResponse authResponse =
            new AuthResponse("access-token", "refresh-token", "Bearer", 900L);
    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController controller;

    // ------------------------------------------------------------ register

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("returns 200 with tokens on success")
        void returnsTokensOnSuccess() {
            RegisterRequest request = new RegisterRequest(
                    "user", "user@example.com", "Pass123!",
                    "John", "Doe", "2000-01-01");
            when(authService.register(request)).thenReturn(authResponse);

            ResponseEntity<AuthResponse> response = controller.register(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().accessToken()).isEqualTo("access-token");
            assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("delegates to authService.register()")
        void delegatesToService() {
            RegisterRequest request = new RegisterRequest(
                    "user", "user@example.com", "Pass123!",
                    "John", "Doe", "2000-01-01");
            when(authService.register(request)).thenReturn(authResponse);

            controller.register(request);

            verify(authService, times(1)).register(request);
        }
    }

    // --------------------------------------------------------------- login

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("returns 200 with tokens on success")
        void returnsTokensOnSuccess() {
            LoginRequest request = new LoginRequest("user", "Pass123!");
            when(authService.login(request)).thenReturn(authResponse);

            ResponseEntity<AuthResponse> response = controller.login(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().accessToken()).isEqualTo("access-token");
            assertThat(response.getBody().refreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("delegates to authService.login()")
        void delegatesToService() {
            LoginRequest request = new LoginRequest("user", "Pass123!");
            when(authService.login(request)).thenReturn(authResponse);

            controller.login(request);

            verify(authService).login(request);
        }
    }

    // ------------------------------------------------------------- refresh

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("returns 200 with new token pair")
        void returnsNewTokenPair() {
            RefreshRequest request = new RefreshRequest("old-refresh-token");
            AuthResponse newPair = new AuthResponse("new-access", "new-refresh", "Bearer", 900L);
            when(authService.refresh(request)).thenReturn(newPair);

            ResponseEntity<AuthResponse> response = controller.refresh(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().accessToken()).isEqualTo("new-access");
            assertThat(response.getBody().refreshToken()).isEqualTo("new-refresh");
        }
    }

    // -------------------------------------------------------------- logout

    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("returns 200 with message and calls authService.logout()")
        void returnsMessageAndDelegates() {
            RefreshRequest request = new RefreshRequest("some-refresh-token");
            doNothing().when(authService).logout("some-refresh-token");

            ResponseEntity<MessageResponse> response = controller.logout(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().message()).isEqualTo("Logged out successfully");
            verify(authService).logout("some-refresh-token");
        }
    }
}