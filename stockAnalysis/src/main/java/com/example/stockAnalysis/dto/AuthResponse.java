package com.example.stockAnalysis.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String message;
}
