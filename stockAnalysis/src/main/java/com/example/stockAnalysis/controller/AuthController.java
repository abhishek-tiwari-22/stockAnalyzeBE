package com.example.stockAnalysis.controller;

import com.example.stockAnalysis.dto.AuthResponse;
import com.example.stockAnalysis.dto.LoginRequest;
import com.example.stockAnalysis.dto.UserRequest;
import com.example.stockAnalysis.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRequest userRequest) {
        AuthResponse response = userService.registerUser(userRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = userService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestHeader("Authorization") String token) {
        userService.logoutUser(token);
        return ResponseEntity.ok("User logged out successfully");
    }
}