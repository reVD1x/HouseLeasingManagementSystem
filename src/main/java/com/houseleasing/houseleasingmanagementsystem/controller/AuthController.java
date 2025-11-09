package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.dto.AuthRequest;
import com.houseleasing.houseleasingmanagementsystem.dto.AuthResponse;
import com.houseleasing.houseleasingmanagementsystem.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 简单的口令验证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private TokenUtil tokenUtil;

    @Value("${app.access-password:admin123}")
    private String accessPassword;

    /**
     * 验证口令并返回token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        if (request.getPassword() != null && request.getPassword().equals(accessPassword)) {
            String token = tokenUtil.generateToken();
            return ResponseEntity.ok(new AuthResponse(true, "验证成功", token));
        }
        return ResponseEntity.ok(new AuthResponse(false, "口令错误", null));
    }

    /**
     * 验证token是否有效
     */
    @PostMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenUtil.validateToken(token)) {
                return ResponseEntity.ok(new AuthResponse(true, "Token有效", token));
            }
        }
        return ResponseEntity.ok(new AuthResponse(false, "Token无效或已过期", null));
    }
}

