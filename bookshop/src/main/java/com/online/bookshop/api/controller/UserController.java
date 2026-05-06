package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.UserService;
import com.online.bookshop.domain.model.Review;
import com.online.bookshop.domain.model.User;
import com.online.bookshop.domain.model.enums.UserStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.notFound;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUserInfo(Authentication auth) {
        return userService.findByUsername(auth.getName())
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
                userService.findAll().stream().map(UserResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @GetMapping("/username")
    public ResponseEntity<UserResponse> getUserByUsername(@RequestParam String username) {
        return userService.findByUsername(username)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @GetMapping("/status")
    public ResponseEntity<List<UserResponse>> getUsersByStatus(@RequestParam UserStatus status) {
        return ResponseEntity.ok(
                userService.findByStatus(status).stream().map(UserResponse::from).toList());
    }

    @GetMapping("/searchByUsernameOrEmail")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(
                userService.findByUsernameOrEmailContaining(query)
                        .stream()
                        .map(UserResponse::from)
                        .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            Authentication auth) {

        User current = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!current.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        current.setEmail(request.email());
        User updated = userService.save(current);
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication auth) {
        User current = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!current.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record UpdateUserRequest(String email) {
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long id) {
        List<Review> reviews = userService.getReviewsByUserId(id);
        if (reviews.isEmpty()) {
            return notFound().build();
        }
        return ResponseEntity.ok(reviews);
    }

    public record UserResponse(
            Long id,
            String username,
            String email,
            String status,
            String registrationDate
    ) {
        public static UserResponse from(User u) {
            return new UserResponse(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getStatus() != null ? u.getStatus().name() : null,
                    u.getRegistrationDate() != null ? u.getRegistrationDate().toString() : null
            );
        }
    }
}
