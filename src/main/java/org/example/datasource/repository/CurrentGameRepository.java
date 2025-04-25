package org.example.datasource.repository;

import org.example.datasource.model.CurrentGameEntity;
import org.example.datasource.model.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrentGameRepository extends CrudRepository<CurrentGameEntity, UUID> {
    List<CurrentGameEntity> findByStateAndGameMode(String state, String gameMode);

    List<CurrentGameEntity> findByPlayerIdAndGameModeAndStateIn(
            UUID playerId, String gameMode, List<String> states);

    @Query("SELECT g FROM CurrentGameEntity g WHERE (g.xPlayer = :playerId OR g.oPlayer = :playerId) " +
            "AND g.gameMode = :gameMode AND g.state IN :states")
    List<CurrentGameEntity> findCompletedGames(
            @Param("playerId") UUID playerId,
            @Param("gameMode") String gameMode,
            @Param("states") List<String> states
    );

    @Query(value = """
    SELECT 
        u.id AS user_id,
        CASE 
            WHEN COUNT(CASE WHEN cg.game_mode = 'Игра с человеком' THEN 1 END) = 0 THEN 0
            ELSE ROUND(
                CAST(
                    COUNT(CASE 
                        WHEN cg.state = 'Победа игрока' 
                        AND cg.game_mode = 'Игра с человеком' 
                        AND cg.player_id = u.id 
                        THEN 1 
                    END) AS NUMERIC
                ) / 
                NULLIF(CAST(
                    COUNT(CASE WHEN cg.game_mode = 'Игра с человеком' THEN 1 END) AS NUMERIC
                ), 0),
                4
            )
        END AS win_ratio
    FROM tictactoe.users u
    LEFT JOIN tictactoe.current_game cg 
        ON u.id = cg.x_player OR u.id = cg.o_player
    GROUP BY u.id
    HAVING COUNT(CASE WHEN cg.game_mode = 'Игра с человеком' THEN 1 END) > 0
    ORDER BY win_ratio DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findBestUsers(@Param("limit") int limit);
}