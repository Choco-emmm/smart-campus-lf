package com.choco.smartlf.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String JWT_SECRET = "lf-jwt-secret-key-for-hmac-sha256-algorithm";//密钥
    public static final long JWT_EXPIRATION = 3600*1000;//token过期时间
    private static final Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    public static String buildJwtToken(Map<String, Object> claims){
        return Jwts.builder()
                .addClaims(claims)
                .signWith(key)
                .compact();
    }

    /**
     * 创建JWT
     * @param userId
     * @param role
     * @param username
     * @return
     */
    public static String createJwtToken(Long userId, Integer role, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("username", username);

        return buildJwtToken(claims);
    }
    public static Claims parseJwt(String jwt){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}
