package com.learn.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Gateway 专用的 JWT 工具类
 *
 * 注意：这个类和 user-service 的 JwtUtil 使用相同的密钥，
 * 这样两边生成的 Token 才能互相验证。
 *
 * 实际项目中，密钥应该从 Nacos 配置中心读取，而不是写死。
 * 这里为了学习方便直接保持一致。
 *
 * @author MangoPie
 */
public class JwtUtil {

    private static final String SECRET = "springcloud-learn-jwt-secret-key-for-auth-demo";

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析 Token
     *
     * @param token JWT Token（不含 "Bearer " 前缀）
     * @return Claims（包含 userId、username 等信息）
     * @throws io.jsonwebtoken.ExpiredJwtException Token 已过期
     * @throws io.jsonwebtoken.JwtException        Token 无效
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
