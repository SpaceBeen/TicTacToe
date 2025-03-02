package org.example.datasource.reposiroty;

import org.example.datasource.model.CurrentGameEntity;
import org.example.domain.model.CurrentGame;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

public interface CurrentGameRepository {
    CurrentGame findById(String gameId);

    void save(CurrentGame game);
}
