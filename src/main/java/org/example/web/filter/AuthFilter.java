package org.example.web.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.domain.service.JwtAuthentication;
import org.example.domain.service.JwtUtil;
import org.example.web.provider.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private final JwtProvider jwtProvider;
    private final JwtUtil jwtUtil;

    public AuthFilter(JwtProvider jwtProvider, JwtUtil jwtUtil) {
        this.jwtProvider = jwtProvider;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (token.isEmpty()) {
                logger.warn("Empty token in Authorization header for URI: {}", request.getRequestURI());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Empty token");
                return;
            }

            try {
                if (jwtProvider.validateAccessToken(token)) {
                    logger.debug("Valid token for URI: {}", request.getRequestURI());
                    Claims claims = jwtProvider.getClaims(token);
                    JwtAuthentication jwtAuthentication = jwtUtil.createJwtAuthentication(claims);
                    SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
                } else {
                    logger.warn("Invalid token for URI: {}", request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token");
                    return;
                }
            } catch (Exception e) {
                logger.error("Token processing error for URI: {}: {}", request.getRequestURI(), e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token processing error: " + e.getMessage());
                return;
            }
        } else {
            logger.debug("No valid Authorization header for URI: {}", request.getRequestURI());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/auth/login") || path.equals("/auth/signup") ||
                path.equals("/auth/refresh-access") || path.equals("/auth/refresh-token");
    }
}