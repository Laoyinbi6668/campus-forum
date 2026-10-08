
package com.example.forum.service;

import com.example.forum.dto.request.LoginRequest;
import com.example.forum.dto.request.RegisterRequest;
import com.example.forum.dto.response.LoginResponse;
import com.example.forum.dto.response.UserResponse;
import com.example.forum.entity.User;
import com.example.forum.enums.UserStatus;
import com.example.forum.repository.UserRepository;
import com.example.forum.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole("USER");
        user.setViolationCount(0);

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new RuntimeException("Account is banned");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateTokenForUser(user.getUsername(), user.getId(), user.getRole());

        return new LoginResponse(token, "Bearer", user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToResponse(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.fromEntity(user);
    }
}
