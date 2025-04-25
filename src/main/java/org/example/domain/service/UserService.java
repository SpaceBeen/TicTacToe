package org.example.domain.service;

import org.example.datasource.mapper.UserMapper;
import org.example.datasource.model.UserEntity;
import org.example.datasource.repository.UserRepository;
import org.example.domain.model.Role;
import org.example.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registerUser(User user) {
        if (userRepository.findByLogin(user.getLogin()).isPresent()) {
            return false;
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Collections.singletonList(Role.USER));
        }
        UserEntity entity = userMapper.toEntity(user);
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(entity);
        return true;
    }

    public User findByLoginAndPassword(String login, String password) {
        return userRepository.findByLogin(login)
                .filter(entity -> passwordEncoder.matches(password, entity.getPassword()))
                .map(userMapper::toDomain)
                .orElse(null);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login)
                .map(userMapper::toDomain)
                .orElse(null);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDomain)
                .orElse(null);
    }
}