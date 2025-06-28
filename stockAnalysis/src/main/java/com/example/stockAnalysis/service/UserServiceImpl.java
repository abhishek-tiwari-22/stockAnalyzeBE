package com.example.stockAnalysis.service;

import com.example.stockAnalysis.dto.AuthResponse;
import com.example.stockAnalysis.dto.LoginRequest;
import com.example.stockAnalysis.dto.UserRequest;
import com.example.stockAnalysis.model.User;
import com.example.stockAnalysis.repository.UserRepository;
import com.example.stockAnalysis.service.UserService;
import com.example.stockAnalysis.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    public AuthResponse registerUser(UserRequest userRequest) {
        // Check if user already exists
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setRoles(Arrays.asList("ROLE_USER"));
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        user.setFavoriteStocks(new HashSet<>());

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String jwt = tokenProvider.generateToken(savedUser.getUsername());

        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setMessage("User registered successfully");

        return response;
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        String jwt = tokenProvider.generateToken(authentication.getName());

        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setMessage("Login successful");

        return response;
    }

    @Override
    public void logoutUser(String token) {
        // In a real application, you might want to blacklist the token
        // For now, we'll just log the logout attempt
        System.out.println("User logged out with token: " + token);
    }
}

