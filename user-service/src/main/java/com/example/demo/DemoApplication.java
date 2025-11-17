package com.example.demo;

import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // This bean will run on application startup
    @Bean
    CommandLineRunner init(UserService userService) {
        return args -> {
            // Check if admin user already exists
            if (!userService.userExists("admin")) {
                // If not, create the admin user DTO
                UserDetailsDTO adminDto = new UserDetailsDTO();
                adminDto.setUsername("admin");
                adminDto.setPassword("1234");
                adminDto.setRole("ROLE_ADMIN");

                // Call the insert method, which handles the full saga
                try {
                    userService.insert(adminDto);
                    System.out.println(" Admin user created successfully! ");
                } catch (Exception e) {
                    System.err.println(" Failed to create admin user: " + e.getMessage() );
                }
            } else {
                System.out.println("Admin user already exists.");
            }
        };
    }
}