package org.example.datasource.mapper;

import org.example.datasource.model.UserEntity;
import org.example.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .login(user.getLogin())
                .password(user.getPassword())
                .rating(user.getRating() != null ? user.getRating() : 0)
                .currentGameId(user.getCurrentGameId())
                .roles(user.getRoles())
                .build();
    }

    public User toDomain(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setLogin(entity.getLogin());
        user.setPassword(entity.getPassword());
        user.setRating(entity.getRating());
        user.setCurrentGameId(entity.getCurrentGameId());
        user.setRoles(entity.getRoles());
        return user;
    }
}