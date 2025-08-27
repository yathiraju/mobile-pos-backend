package com.pos.controller;

import com.pos.model.Users;
import com.pos.repository.UserRepository;
import com.pos.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (userRepo.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Username already exists");
        }

        // Always default to CASHIER when registering via public API
        String role = "ROLE_CASHIER"; // ⚡ better with ROLE_ prefix

        Users users = new Users();
        users.setUsername(username);
        users.setPassword(passwordEncoder.encode(password)); // ✅ store encoded password
        users.setRole(role);

        userRepo.save(users);
        return ResponseEntity.ok("✅ Users registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");

        return userRepo.findByUsername(username).map(user -> {
            if (passwordEncoder.matches(password, user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "username", user.getUsername(),
                        "role", user.getRole()
                ));
            } else {
                return ResponseEntity.status(401).body("❌ Invalid credentials");
            }
        }).orElse(ResponseEntity.status(401).body("❌ Invalid credentials"));
    }

}
