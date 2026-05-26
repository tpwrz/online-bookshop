package com.online.bookshop.domain.model;

import com.online.bookshop.domain.model.enums.UserRole;
import com.online.bookshop.domain.model.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class User {
    private Long id;
    private UserStatus status;
    private UserRole role;
    private String email;
    private String username;
    private String password;
    private LocalDate registrationDate;
    private Long personId;
    private List<Order> orders = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();

    public User() {
    }
}
