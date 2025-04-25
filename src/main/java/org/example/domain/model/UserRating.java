package org.example.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class UserRating {
    private UUID userId;
    private double winrate;

    public UserRating(UUID userId, double winrate) {
        this.userId = userId;
        this.winrate = winrate;
    }

}
