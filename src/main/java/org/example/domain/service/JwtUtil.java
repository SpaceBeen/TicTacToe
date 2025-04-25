package org.example.domain.service;

import io.jsonwebtoken.Claims;
import org.example.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {
    public JwtAuthentication createJwtAuthentication(Claims claims) {
        JwtAuthentication jwtAuthentication = new JwtAuthentication();
        jwtAuthentication.setUuid(UUID.fromString(claims.get("uuid", String.class)));
        jwtAuthentication.setRoles((List<Role>) claims.get("roles"));
        jwtAuthentication.setAuthenticated(true);
        return jwtAuthentication;
    }
}