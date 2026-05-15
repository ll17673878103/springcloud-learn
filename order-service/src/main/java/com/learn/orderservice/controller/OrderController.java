package com.learn.orderservice.controller;

import com.learn.orderservice.entity.Order;
import com.learn.orderservice.feign.UserClient;
import com.learn.orderservice.mapper.OrderMapper;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final OrderMapper orderMapper;

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
    public Map<String, Object> createOrder(@RequestParam("userId") Long userId, HttpServletRequest request) {

        // 从请求头获取当前 Token 信息（日志用）
        String authHeader = request.getHeader("Authorization");
        log.info("创建订单，用户ID: {}, Authorization: {}", userId,
                authHeader != null ? "已携带" : "未携带");

        // 通过 Feign 调用 user-service 获取用户信息
        // FeignAuthInterceptor 会自动传递 Token
        Map<String, Object> userInfo = userClient.getUser(userId);

        // 检查是否触发了降级（降级时 code=503）
        if (userInfo.containsKey("code") && Integer.valueOf(503).equals(userInfo.get("code"))) {
            return userInfo;
        }

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

    /**
     * 创建订单(分布式事务-TM发起方)
     * @param userId 用户id
     * @param product 商品
     * @param amount 金额
     * @return
     */
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @GetMapping("/create_seata")
    public Map<String, Object> createOrderWithSeata(@RequestParam Long userId, @RequestParam String product,
                                                    @RequestParam BigDecimal amount) {

        log.info("=============创建订单===========");
        log.info("全局事务 XID:{}", RootContext.getXID());
        log.info("参数: userId={}, product={}, amount={}", userId, product, amount);

        // 远程调用user模块的扣款(RM 分支事务)
        log.info("第一步：调用user-service 扣款....");
        Map<String, Object> deductResult = userClient.deductBalance(userId, amount);
        log.info("扣款结果:{}", deductResult);

        // 本地创建订单(本地分支事务) -
        log.info("第二步：创建本地订单...");
        Order order = new Order();
        order.setUserId(userId);
        order.setProduct(product);
        order.setAmount(amount);
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        orderMapper.insert(order);
        log.info("订单创建成功，orderId={}", order.getId());

        log.info("==========创建订单完成==========");
        return Map.of(
                "code", 200,
                "message", "订单创建成功",
                "orderId", order.getId()
                );
    }

    /**
     * 测试回滚：故意抛异常
     */
    @GlobalTransactional(name = "create-order-rollback-tx", rollbackFor = Exception.class)
    @GetMapping("/create-with-error")
    public Map<String, Object> createOrderWithError(
            @RequestParam Long userId,
            @RequestParam String product,
            @RequestParam BigDecimal amount) {

        log.info("========== 创建订单（测试回滚）开始 ==========");
        log.info("全局事务 XID: {}", RootContext.getXID());

        // 1. 远程调用扣款（会成功）
        log.info("第一步：调用 user-service 扣款...");
        Map<String, Object> deductResult = userClient.deductBalance(userId, amount);
        log.info("扣款结果: {}", deductResult);

        // 2. 故意抛异常，触发回滚
        log.info("第二步：故意抛异常，触发回滚...");
        throw new RuntimeException("模拟业务异常，触发分布式事务回滚！");
    }
}