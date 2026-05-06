package com.online.bookshop.infrastructure.persistence;

import com.online.bookshop.domain.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"}), @UniqueConstraint(columnNames = {"username"})})
public class UserEntity {
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status;

    @Setter
    @Getter
    @Email
    @NotBlank
    @Size(max = 100)
    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens.")
    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Getter
    @Setter
    @Column(name = "password", nullable = false)
    private String password;

    @Setter
    @Getter
    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Setter
    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private PersonEntity person;

    @Setter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderEntity> orders = new ArrayList<>();

    @Setter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReviewEntity> reviews = new ArrayList<>();

    public UserEntity() {
    }

    public List<OrderEntity> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public List<ReviewEntity> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    public void addReview(ReviewEntity review) {
        reviews.add(review);
        review.setUser(this);
    }

    public @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens.") String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || !username.matches("^[a-zA-Z0-9._-]{3,50}$")) {
            throw new IllegalArgumentException("Invalid username format.");
        }
        this.username = username;
    }

}
