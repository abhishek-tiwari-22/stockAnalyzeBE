package com.example.stockAnalysis.service;

import com.example.stockAnalysis.dto.AuthResponse;
import com.example.stockAnalysis.dto.LoginRequest;
import com.example.stockAnalysis.dto.UserRequest;

public interface UserService {
    AuthResponse registerUser(UserRequest userRequest);
    AuthResponse loginUser(LoginRequest loginRequest);
    void logoutUser(String token);
}
