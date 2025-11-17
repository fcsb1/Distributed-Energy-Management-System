package com.example.demo.controllers;

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Returns list of all users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.findUsers());
    }

    @Operation(summary = "Create user", description = "Creates new user")
    @ApiResponse(responseCode = "201", description = "User created")
    @PostMapping
    public ResponseEntity<UUID> createUser(@Valid @RequestBody UserDetailsDTO userDTO) {
        UUID id = userService.insert(userDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(id);
    }

    @Operation(summary = "Get user by ID", description = "Returns specific user")
    @ApiResponse(responseCode = "200", description = "User retrieved")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @Operation(summary = "Update user", description = "Updates existing user")
    @ApiResponse(responseCode = "200", description = "User updated")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDetailsDTO userDTO) {
        UserDTO updatedUser = userService.update(id, userDTO);
        return ResponseEntity.ok(updatedUser); // 200 OK
    }

    @Operation(summary = "Delete user", description = "Deletes user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}