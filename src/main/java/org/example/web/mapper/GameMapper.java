package org.example.web.mapper;

import org.example.domain.model.CurrentGame;
import org.example.domain.model.GameMode;
import org.example.web.model.GameDTO;
import org.springframework.stereotype.Component;

import java.sql.Date;

@Component
public class GameMapper {

    public GameDTO toGameDTO(CurrentGame game) {
        return GameDTO.builder()
                .id(game.getId())
                .gameBoard(game.getGameField() != null ? game.getGameField().getField() : null)
                .status(game.getState().getDisplayName())
                .mode(game.getGameMode() == GameMode.HUMAN ? "Игра с человеком" : "Игра с компьютером")
                .xplayer(game.getXPlayer())
                .oplayer(game.getOPlayer())
                .playerId(game.getPlayerId())
                .dateOfCreation((Date) game.getDateOfCreation())
                .build();
    }
}