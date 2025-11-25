package com.example.demo.services;

import com.example.demo.config.RabbitConfig;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.dtos.builders.UserBuilder;
import com.example.demo.entities.User;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    // Replace RestTemplate with RabbitTemplate
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserService(UserRepository userRepository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public List<UserDTO> findUsers() {
        return userRepository.findAll().stream().map(UserBuilder::toUserDTO).collect(Collectors.toList());
    }

    public UserDTO findUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));
        return UserBuilder.toUserDTO(user);
    }

    @Transactional
    public UUID insert(UserDetailsDTO userDTO) {
        User user = UserBuilder.toEntity(userDTO);
        user = userRepository.save(user);

        // Sync: Send message to RabbitMQ
        sendUserEvent("CREATED", user.getId(), userDTO);

        LOGGER.debug("User with id {} was inserted in db", user.getId());
        return user.getId();
    }

    @Transactional
    public UserDTO update(UUID id, UserDetailsDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));

        user.setUsername(userDTO.getUsername());
        user = userRepository.save(user);

        // Sync: Send message to RabbitMQ
        sendUserEvent("UPDATED", user.getId(), userDTO);

        return UserBuilder.toUserDTO(user);
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        userRepository.deleteById(id);

        // Sync: Send message to RabbitMQ
        // For delete, we only need the ID
        UserDetailsDTO dummyDto = new UserDetailsDTO();
        sendUserEvent("DELETED", id, dummyDto);
    }

    private void sendUserEvent(String action, UUID userId, UserDetailsDTO dto) {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("action", action);
            messageMap.put("userId", userId.toString());
            messageMap.put("username", dto.getUsername());
            messageMap.put("password", dto.getPassword()); // Only needed for Auth Service
            messageMap.put("role", dto.getRole());         // Only needed for Auth Service

            String jsonMessage = objectMapper.writeValueAsString(messageMap);

            // Routing Key: user.event.[action] (e.g., user.event.created)
            String routingKey = "user.event." + action.toLowerCase();

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, routingKey, jsonMessage);
            LOGGER.info("Sent user sync event: {}", routingKey);
        } catch (Exception e) {
            LOGGER.error("Failed to send user sync event", e);
            // In a real system, you might want to throw an exception or save to an outbox table
        }
    }
}