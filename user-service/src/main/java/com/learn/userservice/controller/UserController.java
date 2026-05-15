package com.learn.userservice.controller;

import com.learn.userservice.config.NacosConfig;
import com.learn.userservice.entity.User;
import com.learn.userservice.mapper.UserMapper;
import io.seata.core.context.RootContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;


@RestController
//@RefreshScope    // ⬅️ 添加热更新支持
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final NacosConfig nacosConfig;
    private final UserMapper userMapper;


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

    /**
     * 扣减余额（RM 分支事务）
     * 这个方法会被 order-service 通过 Feign 调用
     */
    @GetMapping("/user/deduct")
    public Map<String, Object> deductBalance(@RequestParam Long userId,
                                             @RequestParam BigDecimal amount) {
        log.info("========== 扣款开始 ==========");
        log.info("当前全局事务 XID: {}", RootContext.getXID());
        log.info("参数: userId={}, amount={}", userId, amount);

        // 1. 查询扣款前余额
        User userBefore = userMapper.selectById(userId);
        if (userBefore == null) {
            log.error("用户不存在！userId={}", userId);
            throw new RuntimeException("用户不存在！");
        }
        log.info("扣款前余额: {}", userBefore.getBalance());

        // 2. 校验余额是否足够
        if (userBefore.getBalance().compareTo(amount) < 0) {
            log.error("余额不足！当前余额: {}, 需要扣款: {}", userBefore.getBalance(), amount);
            throw new RuntimeException("余额不足！");
        }

        // 3. 执行扣款（使用自定义 SQL，带余额校验）
        int rows = userMapper.deductBalance(userId, amount);
        if (rows == 0) {
            log.error("扣款失败！可能余额不足或用户不存在");
            throw new RuntimeException("扣款失败！");
        }
        log.info("扣款 SQL 执行结果: {} 行受影响", rows);

        // 4. 查询扣款后余额
        User userAfter = userMapper.selectById(userId);
        log.info("扣款后余额: {}", userAfter.getBalance());

        log.info("========== 扣款完成 ==========");
        return Map.of(
                "code", 200,
                "message", "扣款成功",
                "data", Map.of(
                        "userId", userId,
                        "amount", amount,
                        "beforeBalance", userBefore.getBalance(),
                        "afterBalance", userAfter.getBalance()
                )
        );
    }
}