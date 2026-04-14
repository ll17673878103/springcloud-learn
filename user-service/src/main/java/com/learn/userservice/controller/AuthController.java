package com.learn.userservice.controller;

import com.learn.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器
 * 提供登录接口，返回 JWT Token
 *
 * 测试用，用户名密码写死。生产环境应查数据库验证。
 *
 * @author MangoPie
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 模拟登录接口
     * 实际项目中应查询数据库验证用户名密码
     *
     * 测试账号：admin / 123456
     */
    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        log.info("登录请求，用户名: {}", username);

        // 模拟验证（生产环境查数据库）
        if (!"admin".equals(username) || !"123456".equals(password)) {
            return Map.of("code", 401, "message", "用户名或密码错误");
        }

        // 生成 Token
        String token = JwtUtil.generateToken(1L, username);

        log.info("登录成功，用户: {}, Token 已生成", username);

        return Map.of(
                "code", 200,
                "message", "登录成功",
                "data", Map.of(
                        "token", token,
                        "username", username,
                        "userId", 1
                )
        );
    }
}
