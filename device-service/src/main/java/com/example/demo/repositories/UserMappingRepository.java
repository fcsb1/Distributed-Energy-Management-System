package com.example.demo.repositories;

import com.example.demo.entities.UserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserMappingRepository extends JpaRepository<UserMapping, UUID> {
    //we use the User's ID as the Primary Key
}