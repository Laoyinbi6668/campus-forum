
package com.example.forum.controller;

import com.example.forum.dto.request.LoginRequest;
import com.example.forum.dto.request.RegisterRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.LoginResponse;
import com.example.forum.dto.response.UserResponse;
import com.example.forum.security.JwtTokenProvider;
import com.example.forum.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ApiResponse.success("User registered successfully", response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success("Login successful", response);
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> getProfile(HttpServletRequest request) {
        String jwt = getJwtFromRequest(request);
        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
        UserResponse response = userService.getProfile(userId);
        return ApiResponse.success(response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
