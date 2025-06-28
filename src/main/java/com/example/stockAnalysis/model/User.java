package com.example.stockAnalysis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Set<String> favoriteStocks;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Boolean isActive;
}

