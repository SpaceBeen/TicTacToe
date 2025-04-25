package org.example.web.controller;

import io.jsonwebtoken.Claims;
import org.example.domain.model.CurrentGame;
import org.example.domain.model.User;
import org.example.domain.service.GameService;
import org.example.domain.service.JwtAuthentication;
import org.example.domain.service.UserService;
import org.example.web.mapper.GameMapper;
import org.example.web.model.ErrorResponseDTO;
import org.example.web.model.GameDTO;
import org.example.web.model.UserDTO;
import org.example.web.model.UserRatingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:63342")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final GameService gameService;
    private final GameMapper gameMapper;

    @Autowired
    public UserController(UserService userService, GameService gameService, GameMapper gameMapper) {
        this.userService = userService;
        this.gameService = gameService;
        this.gameMapper = gameMapper;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            JwtAuthentication authentication = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized access to /user/{}: No valid authentication", userId);
                return ResponseEntity.status(401).body(new ErrorResponseDTO("Unauthorized"));
            }

            User user = userService.findById(userUuid);
            if (user == null) {
                logger.warn("User not found for ID: {}", userUuid);
                return ResponseEntity.status(404).body(new ErrorResponseDTO("User not found"));
            }

            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getLogin(),
                    user.getRating(),
                    user.getCurrentGameId(),
                    user.getRoles()
            );
            logger.debug("Returning user info for ID: {}", userUuid);
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid user ID: {}", userId);
            return ResponseEntity.badRequest().body(new ErrorResponseDTO("Invalid user ID"));
        } catch (Exception e) {
            logger.error("Error in /user/{}: {}", userId, e.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponseDTO("Server error: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            JwtAuthentication authentication = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized access to /user/me: No valid authentication");
                return ResponseEntity.status(401).body(new ErrorResponseDTO("Unauthorized"));
            }

            UUID userId = (UUID) authentication.getPrincipal();
            User user = userService.findById(userId);
            if (user == null) {
                logger.warn("User not found for ID: {}", userId);
                return ResponseEntity.status(404).body(new ErrorResponseDTO("User not found"));
            }

            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getLogin(),
                    user.getRating(),
                    user.getCurrentGameId(),
                    user.getRoles()
            );
            logger.debug("Returning current user info for ID: {}", userId);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            logger.error("Error in /user/me: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponseDTO("Server error: " + e.getMessage()));
        }
    }

    @GetMapping("/completedGames")
    public ResponseEntity<?> getCompletedGames() {
        try{
            JwtAuthentication authentication = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized access to /user/me: No valid authentication");
                return ResponseEntity.status(401).body(new ErrorResponseDTO("Unauthorized"));
            }

            UUID userId = (UUID) authentication.getPrincipal();
            User user = userService.findById(userId);
            if (user == null) {
                logger.warn("User not found for ID: {}", userId);
                return ResponseEntity.status(404).body(new ErrorResponseDTO("User not found"));
            }

            List<CurrentGame> games = gameService.getFinishedGames(userId);
            List<GameDTO> dtos = games.stream().map(gameMapper::toGameDTO).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
        }
    }

    @GetMapping("/topList")
    public ResponseEntity<?> getBestPlayers(@RequestParam(defaultValue = "10") int limit){
        try{
            JwtAuthentication authentication = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized access to /user/me: No valid authentication");
                return ResponseEntity.status(401).body(new ErrorResponseDTO("Unauthorized"));
            }

            UUID userId = (UUID) authentication.getPrincipal();
            User user = userService.findById(userId);
            if (user == null) {
                logger.warn("User not found for ID: {}", userId);
                return ResponseEntity.status(404).body(new ErrorResponseDTO("User not found"));
            }

            List<UserRatingDTO> users = gameService.getBestPlayers(limit);
            return ResponseEntity.ok(users);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
        }
    }
}