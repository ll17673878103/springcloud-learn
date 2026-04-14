package com.learn.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 负责生成和验证 Token
 *
 * @author MangoPie
 */
public class JwtUtil {

    /**
     * 密钥（生产环境应从配置中心读取，且足够长）
     * 最少 32 字符（256 bit）
     */
    private static final String SECRET = "springcloud-learn-jwt-secret-key-for-auth-demo";

    /**
     * Token 有效期：2 小时
     */
    private static final long EXPIRATION_MS = 2 * 60 * 60 * 1000L;

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token 字符串
     */
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token，返回 Claims
     *
     * @param token JWT Token
     * @return Claims（包含 userId、username 等信息）
     * @throws io.jsonwebtoken.ExpiredJwtException    Token 已过期
     * @throws io.jsonwebtoken.JwtException           Token 无效
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户ID
     */
    public static Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     */
    public static String getUsername(String token) {
        return parseToken(token).getSubject();
    }
}
