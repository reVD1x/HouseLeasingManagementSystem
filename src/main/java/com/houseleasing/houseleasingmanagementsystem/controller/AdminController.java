 package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.model.User;
import com.houseleasing.houseleasingmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/roles")
    public ResponseEntity<List<String>> roles() {
        // provide a simple role list
        return ResponseEntity.ok(List.of("ADMIN", "MANAGER", "USER"));
    }

    @PostMapping("/users/{userId}/role")
    public ResponseEntity<?> setRole(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null) return ResponseEntity.badRequest().body("role required");
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        u.setRole(role);
        userRepository.save(u);
        return ResponseEntity.ok(u);
    }

    @PostMapping("/backup")
    public ResponseEntity<Map<String, Object>> backup() {
        // simple placeholder that returns counts and timestamp
        Map<String, Object> r = new HashMap<>();
        r.put("timestamp", System.currentTimeMillis());
        r.put("users", userRepository.count());
        return ResponseEntity.ok(r);
    }

    @PostMapping("/restore")
    public ResponseEntity<?> restore(@RequestBody Map<String, Object> payload) {
        // in real app you would validate and restore; here just echo
        return ResponseEntity.ok(Map.of("restored", true, "payloadSize", payload == null ? 0 : payload.size()));
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> settings() {
        Map<String, Object> s = new HashMap<>();
        s.put("maintenanceMode", false);
        s.put("version", "0.0.1");
        return ResponseEntity.ok(s);
    }

    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> settings) {
        // persist if needed. Here just echo back
        return ResponseEntity.ok(settings == null ? Map.of() : settings);
    }
}

