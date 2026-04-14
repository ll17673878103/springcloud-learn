package com.learn.orderservice.controller;

import com.learn.orderservice.feign.UserClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 订单控制器
 *
 * @author MangoPie
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final UserClient userClient;

    /**
     * 创建订单（需要 Token 鉴权）
     *
     * 流程：
     * 1. 用户带着 Token 请求此接口
     * 2. FeignAuthInterceptor 自动把 Token 转发给 user-service
     * 3. user-service 验证 Token 后返回用户信息
     * 4. order-service 拿到用户信息，创建订单
     */
    @GetMapping("/create")
    public Map<String, Object> createOrder(
            @RequestParam("userId") Long userId,
            HttpServletRequest request) {

        // 从请求头获取当前 Token 信息（日志用）
        String authHeader = request.getHeader("Authorization");
        log.info("创建订单，用户ID: {}, Authorization: {}", userId,
                authHeader != null ? "已携带" : "未携带");

        // 通过 Feign 调用 user-service 获取用户信息
        // FeignAuthInterceptor 会自动传递 Token
        Map<String, Object> userInfo = userClient.getUser(userId);

        return Map.of(
                "code", 200,
                "message", "订单创建成功",
                "data", Map.of(
                        "orderId", System.currentTimeMillis(),
                        "userId", userId,
                        "userInfo", userInfo
                )
        );
    }
}