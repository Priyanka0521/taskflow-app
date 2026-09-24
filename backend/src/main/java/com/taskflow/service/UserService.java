package com.taskflow.service;

import com.taskflow.config.JwtUtil;
import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("User already exists with that email");
        }
        User user = new User(
                request.getName().trim(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved, saved.getId());
        return new AuthResponse(token, new UserResponse(saved));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user, user.getId());
        return new AuthResponse(token, new UserResponse(user));
    }

    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponse(user);
    }
}
