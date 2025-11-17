package com.example.demo.services;

import com.example.demo.dtos.RegisterDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.dtos.builders.UserBuilder;
import com.example.demo.entities.User;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    @Value("${service.url.auth}") // Read from application.properties
    private String authServiceUrl;

    @Value("${service.url.device}")
    private String deviceServiceUrl;


    @Autowired
    public UserService(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }
    // Method to check if a user exists
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public List<UserDTO> findUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDTO findUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));
        return UserBuilder.toUserDTO(user);
    }

    @Transactional
    public UUID insert(UserDetailsDTO userDTO) {
        // 1Create the user in user_db
        User user = UserBuilder.toEntity(userDTO);
        user = userRepository.save(user);
        final UUID newUserId = user.getId();

        // 2 Create the credential in credential_db
        RegisterDTO registerDTO = new RegisterDTO(
                user.getUsername(),
                userDTO.getPassword(),
                userDTO.getRole(),
                newUserId
        );

        try {
            // Call auth-service
            restTemplate.postForObject(authServiceUrl + "/register", registerDTO, UUID.class);

            // 3. Register the User ID with device-service (for the mapping table)
            restTemplate.postForObject(deviceServiceUrl + "/devices/users/" + newUserId, null, Void.class);

        } catch (Exception e) {
            // If any downstream call fails, roll back the entire transaction
            LOGGER.error("Failed to sync new user {}. Rolling back.", user.getUsername(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }

        LOGGER.debug("User with id {} was inserted in db", newUserId);
        return newUserId;
    }

    @Transactional
    public UserDTO update(UUID id, UserDetailsDTO userDTO) {
        // 1 Find and update the user in user_db
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));

        // Update local entity
        user.setUsername(userDTO.getUsername());
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was updated in db", user.getId());

        // 2 Prepare DTO to update credential_db
        // We re-use RegisterDTO as it contains all necessary fields
        RegisterDTO registerDTO = new RegisterDTO(
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getRole(),
                id // The User ID
        );
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {//update without changing password
            registerDTO.setPassword(null); //
        }

        try {
            // 3 Call auth-service to sync changes
            // Note: We use restTemplate.put() which doesn't return a body.
            restTemplate.put(authServiceUrl + "/user/" + id, registerDTO);

        } catch (Exception e) {
            // If any downstream call fails, roll back the entire transaction
            LOGGER.error("Failed to sync update for user {}. Rolling back.", id, e);
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }

        LOGGER.debug("User with id {} was synced with auth-service", id);
        return UserBuilder.toUserDTO(user);
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }

        // 1. Delete user from user_db
        userRepository.deleteById(id);

        try {
            // 2. Delete credential from credential_db
            restTemplate.delete(authServiceUrl + "/user/" + id);

            // 3. Delete user mapping from device_db
            restTemplate.delete(deviceServiceUrl + "/devices/users/" + id);

        } catch (Exception e) {
            // If downstream call fails, roll back the entire transaction
            LOGGER.error("Failed to sync delete for user {}. Rolling back.", id, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }

        LOGGER.debug("User with id {} was deleted from all services", id);
    }
}