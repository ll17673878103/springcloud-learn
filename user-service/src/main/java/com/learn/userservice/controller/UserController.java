package com.learn.userservice.controller;

import com.learn.userservice.config.NacosConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
//@RefreshScope    // ⬅️ 添加热更新支持
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final NacosConfig nacosConfig;


    /**
     * 查询用户信息（需要 Token 鉴权）
     * Token 验证通过后，拦截器会将 userId 和 username 存入 request
     */
    @GetMapping("/user/{id}")
    public Map<String, Object> getUser(@PathVariable("id") Long id, HttpServletRequest request) {
        // 从拦截器设置的属性中获取当前登录用户
        Long currentUserId = (Long) request.getAttribute("userId");
        String currentUsername = (String) request.getAttribute("username");

        log.info("查询用户: {}, 当前登录用户: {}", id, currentUsername);

        // 模拟返回用户数据
        return Map.of(
                "code", 200,
                "data", Map.of(
                        "id", id,
                        "name", "用户" + id,
                        "email", "user" + id + "@example.com",
                        "requestedBy", currentUsername
                )
        );
    }

    @GetMapping("/user/list")
    public String listUsers() {
        return "User List: [1, 2, 3]";
    }

    @GetMapping("/config")
    public String getConfig() {
        log.info("env: {}, version: {}", nacosConfig.getEnv(), nacosConfig.getVersion());
        return "env: " + nacosConfig.getEnv() + ", version: " + nacosConfig.getVersion();
    }

        @GetMapping("/test")
    public String test() {
        log.info("nacosConfig: {env:{}, version:{}}", nacosConfig.getEnv(), nacosConfig.getVersion());
        return nacosConfig.getEnv() + " - " + nacosConfig.getVersion();
    }
}