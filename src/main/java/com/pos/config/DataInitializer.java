package com.pos.config;

import com.pos.model.Users;
import com.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${default.admin.username}")
    private String defaultAdminUsername;

    @Value("${default.admin.password}")
    private String defaultAdminPassword;

    @Bean
    CommandLineRunner initDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername(defaultAdminUsername).isEmpty()) {
                Users admin = new Users();
                admin.setUsername(defaultAdminUsername);
                admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                admin.setRole("ADMIN");

                userRepository.save(admin);
                System.out.println("✅ Default admin created: username=" + defaultAdminUsername);
            } else {
                System.out.println("ℹ️ Admin user already exists.");
            }
        };
    }
}
