package org.example.web.model;

import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class GameDTO {
    private UUID id; // ID игры
    private int[][] gameBoard; // Игровое поле (для совместимости)
    private String status; // Состояние игры (например, "DRAW", "PLAYER_WON")
    private String mode; // Режим игры ("Игра с человеком" или "Игра с компьютером")
    private UUID xplayer; // ID игрока X
    private UUID oplayer; // ID игрока O
    private UUID playerId; // ID текущего игрока (для совместимости)
    private Date dateOfCreation; // Дата создания игры

    public GameDTO() {
    }

    private GameDTO(Builder builder) {
        this.id = builder.id;
        this.gameBoard = builder.gameBoard;
        this.status = builder.status;
        this.mode = builder.mode;
        this.xplayer = builder.xplayer;
        this.oplayer = builder.oplayer;
        this.playerId = builder.playerId;
        this.dateOfCreation = builder.dateOfCreation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private int[][] gameBoard;
        private String status;
        private String mode;
        private UUID xplayer;
        private UUID oplayer;
        private UUID playerId;
        private Date dateOfCreation;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder gameBoard(int[][] gameBoard) {
            this.gameBoard = gameBoard;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder xplayer(UUID xplayer) {
            this.xplayer = xplayer;
            return this;
        }

        public Builder oplayer(UUID oplayer) {
            this.oplayer = oplayer;
            return this;
        }

        public Builder playerId(UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder dateOfCreation(Date dateOfCreation) {
            this.dateOfCreation = dateOfCreation;
            return this;
        }

        public GameDTO build() {
            return new GameDTO(this);
        }
    }
}