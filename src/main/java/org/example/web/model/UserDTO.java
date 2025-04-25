package org.example.web.model;

import lombok.Data;
import org.example.domain.model.Role;

import java.util.List;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String login;
    private Integer rating;
    private UUID currentGameId;
    private List<Role> roles;

    public UserDTO() {}

    public UserDTO(UUID id, String login, Integer rating, UUID currentGameId, List<Role> roles) {
        this.id = id;
        this.login = login;
        this.rating = rating;
        this.currentGameId = currentGameId;
        this.roles = roles;
    }
}