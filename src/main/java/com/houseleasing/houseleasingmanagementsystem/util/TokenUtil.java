package com.houseleasing.houseleasingmanagementsystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的Token工具类，用于WebUI口令验证
 */
@Component
public class TokenUtil {

    @Value("${app.secret-key:mySecretKeyForHouseLeasingSystem2024!@#}")
    private String secretKey;

    @Value("${app.token.expiration:86400}") // 默认24小时
    private Long expiration;

    private SecretKey getSigningKey() {
        // 确保密钥长度至少32字节
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问token
     */
    public String generateToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("access", "webui");

        return Jwts.builder()
                .claims(claims)
                .subject("webui-access")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}

