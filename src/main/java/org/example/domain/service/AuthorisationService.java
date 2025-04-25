package org.example.domain.service;

import io.jsonwebtoken.Claims;
import org.example.domain.model.User;
import org.example.web.model.request.JwtRequest;
import org.example.web.model.response.JwtResponse;
import org.example.web.model.request.SignUpRequest;
import org.example.web.provider.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorisationService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthorisationService(UserService userService, AuthenticationManager authenticationManager,
                                JwtProvider jwtProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    public boolean register(SignUpRequest request) {
        if (request == null || request.getLogin() == null || request.getPassword() == null) {
            return false;
        }
        if (request.getLogin().length() < 3 || request.getPassword().length() < 6) {
            return false;
        }
        return userService.registerUser(new User(request.getLogin(), request.getPassword()));
    }

    public JwtResponse login(JwtRequest request) {
        if (request == null || request.getLogin() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Invalid login request");
        }

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid login or password", e);
        }

        User user = userService.findByLogin(request.getLogin());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return new JwtResponse("Bearer", accessToken, refreshToken);
    }

    public JwtResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Claims claims = jwtProvider.getClaims(refreshToken);
        User user = userService.findByLogin(claims.getSubject());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        String newAccessToken = jwtProvider.generateAccessToken(user);
        return new JwtResponse("Bearer", newAccessToken, refreshToken);
    }

    public JwtResponse refreshRefreshToken(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Claims claims = jwtProvider.getClaims(refreshToken);
        User user = userService.findByLogin(claims.getSubject());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);
        return new JwtResponse("Bearer", newAccessToken, newRefreshToken);
    }

    public JwtAuthentication getJwtAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthentication) {
            return (JwtAuthentication) auth;
        }
        return null; // Или выбросить исключение, если требуется
    }
}