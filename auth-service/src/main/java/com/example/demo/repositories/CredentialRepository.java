package com.example.demo.repositories;

import com.example.demo.entities.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByUsername(String username);
    Boolean existsByUserId(UUID userId);
    void deleteByUserId(UUID userId);
    Optional<Credential> findByUserId(UUID userId);
}