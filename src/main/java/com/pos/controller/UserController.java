package com.pos.controller;

import com.pos.model.Users;
import com.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get all users (ADMIN only)
    @GetMapping
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a user by ID
    @GetMapping("/{id}")
    public Users getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Users not found"));
    }

    // Create new users (ADMIN can create cashier/admin accounts)
    @PostMapping
    public Users createUser(@RequestBody Users users) {
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        return userRepository.save(users);
    }

    // Update user role/password
    @PutMapping("/{id}")
    public Users updateUser(@PathVariable Long id, @RequestBody Users updatedUsers) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Users not found"));

        users.setUsername(updatedUsers.getUsername());
        if (updatedUsers.getPassword() != null && !updatedUsers.getPassword().isEmpty()) {
            users.setPassword(passwordEncoder.encode(updatedUsers.getPassword()));
        }
        users.setRole(updatedUsers.getRole());
        return userRepository.save(users);
    }

    // Delete user
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "Users deleted successfully!";
    }
}
